package com.app.webfluxreadfile.controller;

import com.app.webfluxreadfile.model.InvoiceDetail;
import com.app.webfluxreadfile.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
public class InvoiceController {

  @Autowired
  private InvoiceService invoiceService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Void> uploadAndProcessFile(@RequestPart(value = "file") Mono<FilePart> file) throws IOException {
      return invoiceService.insertAllInvoiceRecords().then(Mono.empty());
    }

    @GetMapping(path = "/getFile", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<InvoiceDetail>> getFile() {
      return invoiceService.sendDataToConsumer();
    }
}
