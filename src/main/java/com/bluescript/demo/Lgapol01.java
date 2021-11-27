package com.bluescript.demo;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import io.swagger.annotations.ApiResponses;
import com.bluescript.demo.model.WsHeader;
import com.bluescript.demo.model.ErrorMsg;
import com.bluescript.demo.model.EmVariable;
import com.bluescript.demo.model.Dfhcommarea;
import com.bluescript.demo.model.CaCustomerRequest;
import com.bluescript.demo.model.CaCustsecrRequest;
import com.bluescript.demo.model.CaPolicyRequest;
import com.bluescript.demo.model.CaPolicyCommon;
import com.bluescript.demo.model.CaEndowment;
import com.bluescript.demo.model.CaHouse;
import com.bluescript.demo.model.CaMotor;
import com.bluescript.demo.model.CaCommercial;
import com.bluescript.demo.model.CaClaim;

@Getter
@Setter
@RequiredArgsConstructor
@Log4j2
@Component

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 400, message = "This is a bad request, please follow the API documentation for the proper request format"),
        @io.swagger.annotations.ApiResponse(code = 401, message = "Due to security constraints, your access request cannot be authorized"),
        @io.swagger.annotations.ApiResponse(code = 500, message = "The server/Application is down. Please contact support team.") })

public class Lgapol01 {

    @Autowired
    private WsHeader wsHeader;
    @Autowired
    private ErrorMsg errorMsg;
    @Autowired
    private EmVariable emVariable;
    @Autowired
    private Dfhcommarea dfhcommarea;
    @Autowired
    private CaCustomerRequest caCustomerRequest;
    @Autowired
    private CaCustsecrRequest caCustsecrRequest;
    @Autowired
    private CaPolicyRequest caPolicyRequest;
    @Autowired
    private CaPolicyCommon caPolicyCommon;
    @Autowired
    private CaEndowment caEndowment;
    @Autowired
    private CaHouse caHouse;
    @Autowired
    private CaMotor caMotor;
    @Autowired
    private CaCommercial caCommercial;
    @Autowired
    private CaClaim caClaim;
    private String time1;
    private String date1;
    private boolean inRulesMode = true;
    private String caData;
    private int wsCaHeaderLen = 0;
    private int wsRequiredCaLen = 0;
    private int eibcalen;
    
    private String wsAbstime;
    private String wsDate;
    private String wsTime;

    @Value("${api.lgapbr01.uri}")
    private String lgapbr01_URI;
    @Value("${api.lgapbr01.host}")
    private String lgapbr01_host;
    @Value("${api.lgapdb01.uri}")
    private String lgapdb01_URI;
    @Value("${api.lgapdb01.host}")
    private String lgapdb01_host;
    @Value("${api.LGSTSQ.host}")
    private String LGSTSQ_HOST;
    @Value("${api.LGSTSQ.uri}")
    private String LGSTSQ_URI;
    private String caErrorMsg;

    @PostMapping("/lgapol01")
    private ResponseEntity<Dfhcommarea> MainLine(@RequestBody Dfhcommarea payload) {
        // if (eibcalen == 0) {
        // errorMsg.setEmVariable(" NO COMMAREA RECEIVED");
        // writeErrorMessage();
        // log.error("Error code :, LGCA");
        // throw new RuntimeException("LGCA");

        // }
        BeanUtils.copyProperties(payload, dfhcommarea);
        dfhcommarea.setCaReturnCode(00);
        emVariable.setEmCusnum(String.valueOf(dfhcommarea.getCaCustomerNum()));
        wsRequiredCaLen = wsCaHeaderLen + wsRequiredCaLen;
        if (eibcalen < wsRequiredCaLen) {
            dfhcommarea.setCaReturnCode(98); /* return */

        }
        WebClient webclientBuilder = WebClient.create(lgapbr01_host);
        WebClient webclientBuilder2 = WebClient.create(lgapdb01_host);

        if (inRulesMode && dfhcommarea.getCaRequestId() == "01AEND") {
            try {
                Mono<Dfhcommarea> lgapbr01Resp = webclientBuilder.post().uri(lgapbr01_URI)
                        .body(Mono.just(dfhcommarea), Dfhcommarea.class).retrieve().bodyToMono(Dfhcommarea.class)
                        .timeout(Duration.ofMillis(10_000));
                dfhcommarea = lgapbr01Resp.block();
            } catch (Exception e) {
                log.error(e);
            }

        }
        try {
            Mono<Dfhcommarea> lgapdb01Resp = webclientBuilder2.post().uri(lgapdb01_URI)
                    .body(Mono.just(dfhcommarea), Dfhcommarea.class).retrieve().bodyToMono(Dfhcommarea.class)
                    .timeout(Duration.ofMillis(10_000));
            dfhcommarea = lgapdb01Resp.block();
        } catch (Exception e) {
            log.error(e);
        }
        return new ResponseEntity<>(dfhcommarea,HttpStatus.OK);
    }
    /* return */

    public void writeErrorMessage() {
        log.debug("MethodwriteErrorMessagestarted..");
        wsAbstime = LocalTime.now().toString();
        wsAbstime = LocalTime.now().toString();
        wsDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMDDYYYY"));
        wsTime = LocalTime.now().toString();
        String emDate = wsDate.substring(0, 8);
        String emTime = wsTime.substring(0, 6);
        WebClient webclientBuilder3 = WebClient.create(LGSTSQ_HOST);
        try {

            Mono<ErrorMsg> lgstsqResp = webclientBuilder3.post().uri(LGSTSQ_URI)
                    .body(Mono.just(errorMsg), ErrorMsg.class).retrieve().bodyToMono(ErrorMsg.class)
                    .timeout(Duration.ofMillis(10_000));
            errorMsg = lgstsqResp.block();
        } catch (Exception e) {
            log.error(e);
        }
        if (eibcalen > 0) {
            if (eibcalen < 91) {
                caData = (dfhcommarea.getCaRequestId().substring(0, 9));
                try {
                    Mono<String> lgstsqResp = webclientBuilder3.post().uri(LGSTSQ_URI)
                            .body(Mono.just(caErrorMsg), String.class).retrieve().bodyToMono(String.class)
                            .timeout(Duration.ofMillis(10_000));
                    caErrorMsg = lgstsqResp.block();
                } catch (Exception e) {
                    log.error(e);
                }

            } else {
                caData = (dfhcommarea.getCaRequestId() + dfhcommarea.getCaReturnCode() + dfhcommarea.getCaCustomerNum()
                        + dfhcommarea.getCaRequestSpecific().substring(0, 72));
                try {
                    Mono<String> lgstsqResp = webclientBuilder3.post().uri(LGSTSQ_URI)
                            .body(Mono.just(caErrorMsg), String.class).retrieve().bodyToMono(String.class)
                            .timeout(Duration.ofMillis(10_000));
                    caErrorMsg = lgstsqResp.block();
                } catch (Exception e) {
                    log.error(e);
                }

            }

        }

        log.debug("Method writeErrorMessage completed..");
    }

    /* End of program */
}