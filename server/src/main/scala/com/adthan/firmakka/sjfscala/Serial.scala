package com.adthan.firmakka.sjfscala

import jssc._
import java.lang.reflect.Method

object Serial {
  def list: Array[String] = SerialPortList.getPortNames
}

class Serial(var parent: AnyRef, var portName: String, var baudRate: Int,
             var parityChar: Char, var dataBits: Int, var stopBits: Float) extends SerialPortEventListener {
  def this(parent: AnyRef) {
    this(parent, "COM1", 9600, 'N', 8, 1)
  }

  def this(parent: AnyRef, baudRate: Int) {
    this(parent, "COM1", baudRate, 'N', 8, 1)
  }

  def this(parent: AnyRef, portName: String) {
    this(parent, portName, 9600, 'N', 8, 1)
  }

  def this(parent: AnyRef, portName: String, baudRate: Int) {
    this(parent, portName, baudRate, 'N', 8, 1)
  }


  var port: SerialPort = null
  var serialAvailableMethod: Method = null
  var serialEventMethod: Method = null
  var buffer: Array[Byte] = new Array[Byte](32768)
  var inBuffer: Int = 0
  var readOffset: Int = 0
  var bufferUntilSize: Int = 1
  var bufferUntilByte: Byte = 0
  @volatile
  var invokeSerialAvailable: Boolean = false
  var parity = 0
  parityChar match {
    case 'O' => parity = SerialPort.PARITY_ODD
    case 'E' => parity = SerialPort.PARITY_EVEN
    case 'M' => parity = SerialPort.PARITY_MARK
    case 'S' => SerialPort.PARITY_SPACE
    case _ => parity = SerialPort.PARITY_NONE
  }
  var stopBitsIdx: Int = SerialPort.STOPBITS_1
  if (stopBits == 1.5f) stopBitsIdx = SerialPort.STOPBITS_1_5
  else if (stopBits == 2) stopBitsIdx = SerialPort.STOPBITS_2
  port = new SerialPort(portName)
  try {
    port.openPort
    port.setParams(baudRate, dataBits, stopBitsIdx, parity)
    port.addEventListener(this, SerialPort.MASK_RXCHAR)
  } catch {
    case e: SerialPortException =>
      throw new RuntimeException("Error opening serial port " + e.getPortName + ": " + e.getExceptionType)
  }
  try {
    // var params = Array(Class)(ClassTag)
    serialEventMethod = parent.getClass.getMethod("serialEvent", classOf[Array[Serial]])
  }
  catch {
    case e: Exception =>
  }
  try {
    serialAvailableMethod = parent.getClass.getMethod("serialAvailable", classOf[Array[Serial]])
  }
  catch {
    case e: Exception =>
  }

  def pre() = {
    if (serialAvailableMethod != null && invokeSerialAvailable) {
      invokeSerialAvailable = false
      try {
        serialAvailableMethod.invoke(parent, Array[AnyRef](this))
      }
      catch {
        case e: Exception =>
          println("Error, disabling serialAvailable() for " + port.getPortName)
          println(e.getLocalizedMessage)
          serialAvailableMethod = null
      }
    }
  }

  def available: Int = inBuffer - readOffset

  def csetBufferSize(size: Int) = {
    bufferUntilSize = size
  }

  def bufferUntil(inByte: Int) = {
    bufferUntilSize = 0
    bufferUntilByte = inByte.asInstanceOf[Byte]
  }

  def clear() = {
    buffer synchronized {
      inBuffer = 0
      readOffset = 0
    }
  }

  def getCTS: Boolean = {
    try {
      port.isCTS
    }
    catch {
      case e: SerialPortException =>
        throw new RuntimeException("Error reading the CTS line: " + e.getExceptionType)
    }
  }

  def getDSR: Boolean = {
    try {
      port.isDSR
    }
    catch {
      case e: SerialPortException =>
        throw new RuntimeException("Error reading the DSR line: " + e.getExceptionType)

    }
  }

  def last: Int = {
    if (inBuffer == readOffset) -1
    buffer synchronized {
      val ret: Int = buffer(inBuffer - 1) & 0xFF
      inBuffer = 0
      readOffset = 0
      return ret
    }
  }

  def lastChar: Char = last.asInstanceOf[Char]

  def read: Int = {
    if (inBuffer == readOffset) -1
    buffer synchronized {
      val ret: Int = buffer({
        readOffset += 1
        readOffset - 1
      }) & 0xFF
      if (inBuffer == readOffset) {
        inBuffer = 0
        readOffset = 0
      }
      return ret
    }
  }

  def readBytes: Array[Byte] = {
    if (inBuffer == readOffset) {
      return null
    }
    buffer synchronized {
      val ret: Array[Byte] = new Array[Byte](inBuffer - readOffset)
      System.arraycopy(buffer, readOffset, ret, 0, ret.length)
      inBuffer = 0
      readOffset = 0
      return ret
    }
  }

  def readBytes(dest: Array[Byte]): Int = {
    if (inBuffer == readOffset) {
      return 0
    }
    buffer synchronized {
      var toCopy: Int = inBuffer - readOffset
      if (dest.length < toCopy) {
        toCopy = dest.length
      }
      System.arraycopy(buffer, readOffset, dest, 0, toCopy)
      readOffset += toCopy
      if (inBuffer == readOffset) {
        inBuffer = 0
        readOffset = 0
      }
      return toCopy
    }
  }

  def readBytesUntil(inByte: Int): Array[Byte] = {
    if (inBuffer == readOffset) {
      return null
    }
    buffer synchronized {
      //var found: Int = -1
      var i: Int = readOffset
      val found = buffer.indexOf(inByte.asInstanceOf[Byte])
      if (found == -1) {
        return null
      }
      val toCopy: Int = found - readOffset + 1
      val dest: Array[Byte] = new Array[Byte](toCopy)
      System.arraycopy(buffer, readOffset, dest, 0, toCopy)
      readOffset += toCopy
      if (inBuffer == readOffset) {
        inBuffer = 0
        readOffset = 0
      }
      return dest
    }
  }

  def readBytesUntil(inByte: Int, dest: Array[Byte]): Int = {
    if (inBuffer == readOffset) 0
    buffer synchronized {
      var i: Int = readOffset
      val found = buffer.indexOf(inByte.asInstanceOf[Byte])
      if (found == -1) {
        return 0
      }
      val toCopy: Int = found - readOffset + 1
      if (dest.length < toCopy) {
        println("The buffer passed to readBytesUntil() is to small " + "to contain " +
          toCopy + " bytes up to and including " + "char " + inByte.asInstanceOf[Byte])
        -1
      }
      System.arraycopy(buffer, readOffset, dest, 0, toCopy)
      readOffset += toCopy
      if (inBuffer == readOffset) {
        inBuffer = 0
        readOffset = 0
      }
      toCopy
    }
  }

  def readChar: Char = read.asInstanceOf[Char]

  def readString: String = {
    if (inBuffer == readOffset) null
    new String(readBytes)
  }

  def readStringUntil(inByte: Int): String = {
    val temp = readBytesUntil(inByte)
    if (temp == null) null
    else new String(temp)
  }

  def serialEvent(event: SerialPortEvent) {
    if (event.getEventType == SerialPortEvent.RXCHAR) {
      var toRead: Int = 0
      try {
        while (0 < {
          toRead = port.getInputBufferBytesCount
          toRead
        }) {
          buffer synchronized {
            if (serialEventMethod != null) {
              toRead = 1
            }
            if (buffer.length < inBuffer + toRead) {
              val temp = new Array[Byte](buffer.length << 1)
              System.arraycopy(buffer, 0, temp, 0, inBuffer)
              buffer = temp
            }
            val read = port.readBytes(toRead)
            System.arraycopy(read, 0, buffer, inBuffer, read.length)
            inBuffer += read.length
          }
          if (serialEventMethod != null) {
            if ((0 < bufferUntilSize && bufferUntilSize <= inBuffer - readOffset) || (0 == bufferUntilSize && bufferUntilByte == buffer(inBuffer - 1))) {
              try {
                serialEventMethod.invoke(parent, Array[AnyRef](this))
              }
              catch {
                case e: Exception =>
                  println("Error, disabling serialEvent() for " + port.getPortName)
                  println(e.getLocalizedMessage)
                  serialEventMethod = null
              }
            }
          }
          invokeSerialAvailable = true
        }
      }
      catch {
        case e: SerialPortException =>
          throw new RuntimeException("Error reading from serial port " + e.getPortName + ": " + e.getExceptionType)
      }
    }
  }

  def setDTR(state: Boolean) {
    try {
      port.setDTR(state)
    }
    catch {
      case e: SerialPortException =>
        throw new RuntimeException("Error setting the DTR line: " + e.getExceptionType)
    }
  }

  def setRTS(state: Boolean) {
    try {
      port.setRTS(state)
    }
    catch {
      case e: SerialPortException =>
        throw new RuntimeException("Error setting the RTS line: " + e.getExceptionType)
    }
  }

  def stop() = {
    try {
      port.closePort
    }
    catch {
      case e: SerialPortException =>
    }
    inBuffer = 0
    readOffset = 0
  }

  def write(src: Array[Byte]) {
    try {
      port.writeBytes(src)
    }
    catch {
      case e: SerialPortException =>
        throw new RuntimeException("Error writing to serial port " + e.getPortName + ": " + e.getExceptionType)
    }
  }

  def write(src: Int) {
    try {
      port.writeInt(src)
    }
    catch {
      case e: SerialPortException =>
        throw new RuntimeException("Error writing to serial port " + e.getPortName + ": " + e.getExceptionType)
    }
  }

  def write(src: String) {
    try {
      port.writeString(src)
    }
    catch {
      case e: SerialPortException =>
        throw new RuntimeException("Error writing to serial port " + e.getPortName + ": " + e.getExceptionType)
    }
  }
}
