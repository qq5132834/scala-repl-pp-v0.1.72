package replpp.server

import dotty.tools.dotc.config.Printers.config
import dotty.tools.repl.State
import org.slf4j.{Logger, LoggerFactory}
import replpp.Colors.BlackWhite
import replpp.{Config, ReplDriverBase, pwd}

import java.io.*
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.{BlockingQueue, Executors, LinkedBlockingQueue, Semaphore}
import scala.concurrent.duration.Duration
import scala.concurrent.impl.Promise
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.{Failure, Success}

class EmbeddedRepl(predefLines: IterableOnce[String] = Seq.empty) {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  /** repl and compiler output ends up in this replOutputStream. repl和编译器输出最终在这个replOutputStream中。 */
  private val replOutputStream = new ByteArrayOutputStream()

  private val replDriver: ReplDriver = {
    val inheritedClasspath = System.getProperty("java.class.path")
    val compilerArgs = Array(
      "-classpath", inheritedClasspath,
      "-explain", // verbose scalac error messages
      "-deprecation",
      "-color", "never"
    )
    new ReplDriver(compilerArgs, new PrintStream(replOutputStream), classLoader = None)
  }

  private var state: State = {
    val state = replDriver.execute(predefLines)(using replDriver.initialState)
    val output = readAndResetReplOutputStream()
    if (output.nonEmpty)
      logger.info(output)
    state
  }

  private val singleThreadedJobExecutor: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  /** Execute `inputLines` in REPL (in single threaded ExecutorService) and provide Future for result callback. 在REPL（在单线程ExecutorService中）中执行“inputLines”，并为结果回调提供Future。 */
  def queryAsync(code: String): (UUID, Future[String]) =
    queryAsync(code.linesIterator)

  /** Execute `inputLines` in REPL (in single threaded ExecutorService) and provide Future for result callback. 在REPL（在单线程ExecutorService中）中执行“inputLines”，并为结果回调提供Future。 */
  def queryAsync(inputLines: IterableOnce[String]): (UUID, Future[String]) = {
    val uuid = UUID.randomUUID()
    val future = Future {
      state = replDriver.execute(inputLines)(using state)
      readAndResetReplOutputStream()
    } (using singleThreadedJobExecutor)

    (uuid, future)
  }

  private def readAndResetReplOutputStream(): String = {
    val result = replOutputStream.toString(StandardCharsets.UTF_8)
    replOutputStream.reset()
    result
  }

  /** Submit query to the repl, await and return results. 将查询提交给repl，等待并返回结果。 */
  def query(code: String): QueryResult =
    query(code.linesIterator)

  /** Submit query to the repl, await and return results. */
  def query(inputLines: IterableOnce[String]): QueryResult = {
    val (uuid, futureResult) = queryAsync(inputLines)
    val result = Await.result(futureResult, Duration.Inf)
    QueryResult(result, uuid, success = true)
  }

  /** Shutdown the embedded shell and associated threads. 关闭嵌入式shell和相关线程。
    */
  def shutdown(): Unit = {
    logger.info("shutting down")
    singleThreadedJobExecutor.shutdown()
  }
}

class ReplDriver(args: Array[String], out: PrintStream, classLoader: Option[ClassLoader])
  extends ReplDriverBase(args, out, maxHeight = None, classLoader)(using BlackWhite) {
  def execute(inputLines: IterableOnce[String])(using state: State = initialState): State =
    interpretInput(inputLines, state, pwd)
}
