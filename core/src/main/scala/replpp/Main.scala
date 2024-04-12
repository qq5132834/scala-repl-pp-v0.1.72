package replpp

import replpp.scripting.ScriptRunner

object Main {
  def main(args1: Array[String]): Unit = {
    println("aaabbbccc")
    val args = Array(
                      "--greeting", "welcome to use vision.",             // 问候
                      "--prompt", "vision",                               // Terminal 提示语
                      "--script", "core/src/main/resources/scripts/script.scala"     //执行脚本
                     )
    val config = Config.parse(args)

    if (config.scriptFile.isDefined) {
      //脚本运行
      ScriptRunner.main(args)
    } else {
      //交互式shell运行
      InteractiveShell.run(config)
    }
  }
}
