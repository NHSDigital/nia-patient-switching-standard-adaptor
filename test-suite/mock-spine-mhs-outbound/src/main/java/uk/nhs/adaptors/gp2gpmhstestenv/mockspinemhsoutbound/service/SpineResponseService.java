package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service;

import org.springframework.stereotype.Service;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.SuccessTemplateParams;
import com.github.mustachejava.Mustache;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.util.MustacheUtil;

@Service
public class SpineResponseService {

    private static final Mustache SUCCESS_TEMPLATE = MustacheUtil.loadTemplate("async_reliable_success_response.mustache");

    public String fillSuccessTemplate(SuccessTemplateParams params) {
        return MustacheUtil.fillTemplate(SUCCESS_TEMPLATE, params);
    }
}
