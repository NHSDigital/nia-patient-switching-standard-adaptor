package uk.nhs.adaptors.pss.gpc.config.filter;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;

import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.common.service.MDCService;


@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class ConversationIdFilter extends OncePerRequestFilter {

    private static final String CONVERSATION_ID = "ConversationId";

    private final MDCService mdcService;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
        throws java.io.IOException, ServletException {
        try {
            var token = request.getHeader(CONVERSATION_ID);
            if (isEmpty(token)) {
                token = getRandomCorrelationId();
            }
            mdcService.applyConversationId(token);
            token = encode(token, UTF_8);
            response.addHeader(CONVERSATION_ID, token);
            chain.doFilter(request, response);
        } finally {
            mdcService.resetAllMdcKeys();
        }
    }

    public String getRandomCorrelationId() {
        return randomUUID().toString().toUpperCase();
    }
}
