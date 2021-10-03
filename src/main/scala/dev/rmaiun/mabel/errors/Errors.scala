package dev.rmaiun.mabel.errors

object Errors {

  case class UnavailableUsersFound(users: List[String])
      extends RuntimeException(s"Unable to process rating for players: ${users.mkString(",")}")
}
