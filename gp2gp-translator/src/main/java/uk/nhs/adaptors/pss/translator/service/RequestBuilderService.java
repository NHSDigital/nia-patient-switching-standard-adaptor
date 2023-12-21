package uk.nhs.adaptors.pss.translator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.config.MhsOutboundConfiguration;

@Service
public class RequestBuilderService {

    @Autowired
    private MhsOutboundConfiguration mhsOutboundConfiguration;

    @SneakyThrows
    public SslContext buildSSLContext() {
        return SslContextBuilder.forClient().build();
    }

    public ExchangeStrategies buildExchangeStrategies() {
        return ExchangeStrategies
            .builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(mhsOutboundConfiguration.getMaxRequestSize()))
            .build();
    }
}
