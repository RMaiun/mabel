package dev.rmaiun.mabel.services

import dev.rmaiun.mabel.dtos.ArbiterDto.ListEloPointsDtoOut
import dev.rmaiun.mabel.dtos.EloRatingDto._
import dev.rmaiun.mabel.errors.Errors.UnavailableUsersFound
import dev.rmaiun.mabel.services.ArbiterClient.HasArbiterClient
import zio._

object EloPointsCalculator {
  type HasEloPointsCalculator = Has[EloPointsCalculator.Service]
  val layer: URLayer[HasArbiterClient, HasEloPointsCalculator] = (EloPointsCalculatorService(_)).toLayer

  trait Service {
    def calculate(dto: EloPlayers): Task[UserCalculatedPoints]
  }

  case class EloPointsCalculatorService(
    arbiterClient: ArbiterClient.Service
  ) extends Service {
    override def calculate(dto: EloPlayers): Task[UserCalculatedPoints] =
      for {
        loadedPoints <- loadEloPoints(dto)
      } yield {
        val avgWinPoints = loadedPoints.calculatedEloPoints
          .filter(x => List(dto.w1, dto.w2).contains(x.user))
          .map(_.value)
          .sum / 2
        val avgLosePoints = loadedPoints.calculatedEloPoints
          .filter(x => List(dto.l1, dto.l2).contains(x.user))
          .map(_.value)
          .sum / 2
        val w1 = loadedPoints.calculatedEloPoints.filter(x => x.user == dto.w1).head
        val w2 = loadedPoints.calculatedEloPoints.filter(x => x.user == dto.w2).head
        val l1 = loadedPoints.calculatedEloPoints.filter(x => x.user == dto.l1).head
        val l2 = loadedPoints.calculatedEloPoints.filter(x => x.user == dto.l2).head

        val kW1 = findK(w1.value, w1.gamesPlayed)
        val kW2 = findK(w2.value, w2.gamesPlayed)
        val kL1 = findK(l1.value, l1.gamesPlayed)
        val kL2 = findK(l2.value, l2.gamesPlayed)

        val ratingW1 = eloAlgorithmRunWin(w1.value, avgLosePoints, kW1)
        val ratingW2 = eloAlgorithmRunWin(w2.value, avgLosePoints, kW2)
        val ratingL1 = eloAlgorithmRunLose(avgWinPoints, l1.value, kL1)
        val ratingL2 = eloAlgorithmRunLose(avgWinPoints, l2.value, kL2)
        UserCalculatedPoints(
          CalculatedPoints(w1.user, ratingW1 - w1.value),
          CalculatedPoints(w2.user, ratingW2 - w2.value),
          CalculatedPoints(l1.user, ratingL1 - l1.value),
          CalculatedPoints(l2.user, ratingL2 - l2.value)
        )
      }

    private def loadEloPoints(dto: EloPlayers): Task[ListEloPointsDtoOut] = {
      val users = List(dto.w1, dto.w2, dto.l1, dto.l2)
      arbiterClient.listCalculatedEloPoints(users).flatMap { data =>
        if (data.unratedPlayers.isEmpty) {
          Task.succeed(data)
        } else {
          Task.fail(UnavailableUsersFound(data.unratedPlayers))
        }
      }
    }

    private def eloAlgorithmRunWin(rA: Int, rB: Int, k: Int): Int = {
      val eA           = expectedRating(rB - rA)
      val winnerPoints = rA + k * (1 - eA)
      Math.round(winnerPoints)
    }

    private def eloAlgorithmRunLose(rA: Int, rB: Int, k: Int): Int = {
      val eB          = expectedRating(rA - rB)
      val loserPoints = rB + k * (0 - eB)
      Math.round(loserPoints)
    }

    private def expectedRating(ratingDiff: Float): Float = {
      val ratingDiffDivided = ratingDiff / 400
      val divisor           = Math.pow(10, 1.0f * ratingDiffDivided).toFloat
      1.0f / (1 + divisor)
    }

    private def findK(rating: Int, games: Int): Int =
      if (games <= 30) {
        40
      } else if (rating >= 2400) {
        10
      } else {
        20
      }
  }
}
