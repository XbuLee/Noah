import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/**
 * @author Leexiaobu
 * @date 2021-01-21 17:19
 */
public class Simple {

  public static void main(String[] args) throws InterruptedException {

    System.out.println("simple");
    new Simple().test();
  }

  static SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter()))
      .addSpanProcessor(BatchSpanProcessor.builder(new LoggingSpanExporter()).build())
      .build();
  static OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
      .setTracerProvider(tracerProvider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .buildAndRegisterGlobal();
  static Tracer tracer =
      openTelemetry.getTracer("Simple", "1.0.0");

  public void test() throws InterruptedException {
    Span span = tracer.spanBuilder("test").startSpan();
// put the span into the current Context
    try (Scope scope = span.makeCurrent()) {
      System.out.println("test");
      Thread.sleep(1000);
      System.out.println("test end ");
      test2();
    } catch (Throwable t) {
      span.setStatus(StatusCode.ERROR, "Change it to your error message");
    } finally {
      span.end(); // closing the scope does not end the span, this has to be done manually
    }
  }

  void test2() {
    Span parentSpan = tracer.spanBuilder("parent").startSpan();
    childOne(parentSpan);
    System.out.println("test");
    System.out.println("test end ");
    parentSpan.end();
  }


  void childOne(Span parentSpan) {
    Span childSpan = tracer.spanBuilder("child")
        .setParent(Context.current().with(parentSpan))
        .startSpan();
    // do stuff
    childSpan.end();
  }

}