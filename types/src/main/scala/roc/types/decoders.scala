package roc
package types

import jawn.ast.JParser
import cats.data.{Validated, Xor}
import io.netty.buffer.Unpooled
import java.nio.ByteBuffer
import roc.postgresql.ElementDecoder
import roc.types.failures._

object decoders {

  implicit def optionElementDecoder[A](implicit f: ElementDecoder[A]) = 
    new ElementDecoder[Option[A]] {
      def textDecoder(text: String): Option[A]         = Some(f.textDecoder(text))
      def binaryDecoder(bytes: Array[Byte]): Option[A] = Some(f.binaryDecoder(bytes))
      def nullDecoder(): Option[A]                     = None
    }

  implicit val stringElementDecoder: ElementDecoder[String] = new ElementDecoder[String] {
    def textDecoder(text: String): String         = text
    def binaryDecoder(bytes: Array[Byte]): String = bytes.map(_.toChar).mkString
    def nullDecoder(): String                     = throw new NullDecodedFailure("STRING")
  }

  implicit val shortElementDecoder: ElementDecoder[Short] = new ElementDecoder[Short] {
    def textDecoder(text: String): Short         = Xor.catchNonFatal(
      text.toShort
    ).fold(
      {l => throw new ElementDecodingFailure("SHORT", l)},
      {r => r}
    )
    def binaryDecoder(bytes: Array[Byte]): Short = Xor.catchNonFatal({
      val buffer = Unpooled.directBuffer(2)
      buffer.writeBytes(bytes.take(2))
      buffer.readShort
    }).fold(
      {l => throw new ElementDecodingFailure("SHORT", l)},
      {r => r}
    )
    def nullDecoder(): Short                     = throw new NullDecodedFailure("SHORT")
  }

  implicit val intElementDecoder: ElementDecoder[Int] = new ElementDecoder[Int] {
    def textDecoder(text: String): Int         = Xor.catchNonFatal(
      text.toInt
    ).fold(
      {l => throw new ElementDecodingFailure("INT", l)},
      {r => r}
    )
    def binaryDecoder(bytes: Array[Byte]): Int = Xor.catchNonFatal({
      val buffer = Unpooled.directBuffer(4)
      buffer.writeBytes(bytes.take(4))
      buffer.readInt
    }).fold(
      {l => throw new ElementDecodingFailure("INT", l)},
      {r => r}
    )
    def nullDecoder(): Int =                     throw new NullDecodedFailure("INT")
  }

  implicit val longElementDecoder: ElementDecoder[Long] = new ElementDecoder[Long] {
    def textDecoder(text: String): Long         = Xor.catchNonFatal(
      text.toLong
    ).fold(
      {l => throw new ElementDecodingFailure("LONG", l)},
      {r => r}
    )
    def binaryDecoder(bytes: Array[Byte]): Long = Xor.catchNonFatal({
      val buffer = Unpooled.directBuffer(8)
      buffer.writeBytes(bytes.take(8))
      buffer.readLong
    }).fold(
      {l => throw new ElementDecodingFailure("LONG", l)},
      {r => r}
    )
    def nullDecoder: Long                       = throw new NullDecodedFailure("LONG") 
  }

  implicit val floatElementDecoder: ElementDecoder[Float] = new ElementDecoder[Float] {
    def textDecoder(text: String): Float         = Xor.catchNonFatal(
      text.toFloat
    ).fold(
      {l => throw new ElementDecodingFailure("FLOAT", l)},
      {r => r}
    )
    def binaryDecoder(bytes: Array[Byte]): Float = Xor.catchNonFatal({
      val buffer = Unpooled.directBuffer(4)
      buffer.writeBytes(bytes.take(4))
      buffer.readFloat
    }).fold(
      {l => throw new ElementDecodingFailure("FLOAT", l)},
      {r => r}
    )
    def nullDecoder: Float                       = throw new NullDecodedFailure("FLOAT")
  }

  implicit val doubleElementDecoder: ElementDecoder[Double] = new ElementDecoder[Double] {
    def textDecoder(text: String): Double         = Xor.catchNonFatal(
      text.toDouble
    ).fold(
      {l => throw new ElementDecodingFailure("DOUBLE", l)},
      {r => r}
    )
    def binaryDecoder(bytes: Array[Byte]): Double = Xor.catchNonFatal({
      val buffer = Unpooled.directBuffer(8)
      buffer.writeBytes(bytes.take(8))
      buffer.readDouble
    }).fold(
      {l => throw new ElementDecodingFailure("DOUBLE", l)},
      {r => r}
    )
    def nullDecoder: Double                       = throw new NullDecodedFailure("DOUBLE")
  }

  implicit val booleanElementDecoder: ElementDecoder[Boolean] = new ElementDecoder[Boolean] {
    def textDecoder(text: String): Boolean         = Xor.catchNonFatal(text.head match {
      case 't' => true
      case 'f' => false
    }).fold(
     {l => throw new ElementDecodingFailure("BOOLEAN", l)},
     {r => r}
    )
    def binaryDecoder(bytes: Array[Byte]): Boolean = Xor.catchNonFatal(bytes.head match {
      case 0x00 => false
      case 0x01 => true
    }).fold(
      {l => throw new ElementDecodingFailure("BOOLEAN", l)},
      {r => r}
    )
    def nullDecoder: Boolean                       = throw new NullDecodedFailure("BOOLEAN")
  }

  implicit val charElementDecoder: ElementDecoder[Char] = new ElementDecoder[Char] {
    def textDecoder(text: String): Char         = Xor.catchNonFatal(text.head.toChar).fold(
      {l => throw new ElementDecodingFailure("CHAR", l)},
      {r => r}
    )
    def binaryDecoder(bytes: Array[Byte]): Char = Xor.catchNonFatal(bytes.head.toChar).fold(
      {l => throw new ElementDecodingFailure("CHAR", l)},
      {r => r}
    )
    def nullDecoder: Char                       = throw new NullDecodedFailure("CHAR")
  }

  implicit val jsonElementDecoder: ElementDecoder[Json] = new ElementDecoder[Json] {
    def textDecoder(text: String): Json         = Validated.fromTry(
      JParser.parseFromString(text)
    ).fold(
      {l => throw new ElementDecodingFailure("JSON", l)},
      {r => r }
    )
    def binaryDecoder(bytes: Array[Byte]): Json = Validated.fromTry({
      val buffer = ByteBuffer.wrap(bytes)
      JParser.parseFromByteBuffer(buffer)
    }).fold(
     {l => throw new ElementDecodingFailure("JSON", l)},
     {r => r}
    )
    def nullDecoder: Json                       = throw new NullDecodedFailure("JSON")
  }

}
