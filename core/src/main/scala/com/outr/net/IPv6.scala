package com.outr.net

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class IPv6(part1: Int = 0, part2: Int = 0, part3: Int = 0, part4: Int = 0, part5: Int = 0, part6: Int = 0, part7: Int = 0, part8: Int = 1) extends IP {
  lazy val address = Array(part1, part2, part3, part4, part5, part6, part7, part8)
  lazy val addressString = s"$part1:$part2:$part3:$part4:$part5:$part6:$part7:$part8"
}