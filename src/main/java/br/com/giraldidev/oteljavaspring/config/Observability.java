package br.com.giraldidev.oteljavaspring.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;

@Configuration
public class Observability {

        @Value("${info.app.version:unknown}")
        private String appVersion;

        @Bean
        public OpenTelemetry getOpenTelemetry(Environment environment) {
                Resource resource = Resource.getDefault().toBuilder()
                                .put(ResourceAttributes.SERVICE_NAME, environment.getProperty("OTEL_SERVICE_NAME"))
                                .put(ResourceAttributes.SERVICE_VERSION, this.appVersion).build();

                SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                                .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder()
                                                .setEndpoint(Objects.requireNonNull(
                                                                environment.getProperty("OTEL_EXPORTER_OTLP_ENDPOINT")))
                                                .build()).build())
                                .setResource(resource).build();

                SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
                                .addLogRecordProcessor(BatchLogRecordProcessor.builder(OtlpGrpcLogRecordExporter
                                                .builder()
                                                .setEndpoint(Objects.requireNonNull(
                                                                environment.getProperty("OTEL_EXPORTER_OTLP_ENDPOINT")))
                                                .build()).build())
                                .setResource(resource).build();

                SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                                .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder()
                                                .setEndpoint(Objects.requireNonNull(
                                                                environment.getProperty("OTEL_EXPORTER_OTLP_ENDPOINT")))
                                                .build()).build())
                                .setResource(resource).build();

                OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().setTracerProvider(sdkTracerProvider)
                                .setLoggerProvider(sdkLoggerProvider).setMeterProvider(sdkMeterProvider)
                                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                                .build();

                OpenTelemetryAppender.install(sdk);

                return sdk;
        }
}
