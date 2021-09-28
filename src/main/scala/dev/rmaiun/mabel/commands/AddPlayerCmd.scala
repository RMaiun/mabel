package dev.rmaiun.mabel.commands

import dev.rmaiun.mabel.dtos.ArbiterDto.RealmDto
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class AddPlayerCmd (surname:String, tid:Long, admin:Boolean, moderator:Long)

object AddPlayerCmd{
  implicit val AddPlayerCmdDecoder: Decoder[AddPlayerCmd] = deriveDecoder[AddPlayerCmd]
}
