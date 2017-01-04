package abo.util

import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

import scala.io.Source

object LineIterator {

  type Lines = Iterator[String]

  def fromFile(file: File): Lines = {
    def fromGZipFile: Lines = {
      val in = new GZIPInputStream(new FileInputStream(file))
      Source.fromInputStream(in).getLines()
    }

    if (file.getName.endsWith(".gz")) fromGZipFile
    else Source.fromFile(file).getLines()
  }
}
