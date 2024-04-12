package replpp

import replpp.scripting.ScriptRunner

object Main {
  def main(args: Array[String]): Unit = {
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
