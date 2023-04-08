package com.app.webfluxreadfile.service;

import com.app.webfluxreadfile.model.InvoiceDetail;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class InvoiceService {
  Flux<InvoiceDetail> fileFlux = Flux.empty();

  public Flux<InvoiceDetail> insertAllInvoiceRecords() {
    return fileFlux = Flux.using(
            () -> new FileReader(new File("src/main/resources/csv/initial_data.csv")),
            (reader) -> {
      BufferedReader br = new BufferedReader(reader);
      String row[];
      try {
        row = br.readLine().split(",");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      // CSV has 2 column with same name as "document_create_date". We will be using
      // the "document_create_date.1".
      row[8] = "document_create_date.1";
      // This map the columnName to its index in row array. This would make easy for
      // us to access the column from csv directly using columnName.
      Map<String, Integer> columnNameToIndexMapper = new HashMap<>();
      for (int i = 0; i < row.length; i++) {
        columnNameToIndexMapper.put(row[i], i);
      }
      return insertAllInvoiceRecordsBatch(br, columnNameToIndexMapper);
    }, reader -> Mono.fromRunnable(() -> {
             try {
                reader.close();
              } catch (IOException e) {
                // Handle exception
              }
            }));
  }

  public Flux<InvoiceDetail> insertAllInvoiceRecordsBatch(BufferedReader br, Map<String, Integer> columnNameToIndexMapper) {
    return Flux.fromStream(br.lines()).map(line -> {
      return getInvoiceDetails(line.split(","), columnNameToIndexMapper);
    }).delayElements(Duration.ofSeconds(5));
  }

  private InvoiceDetail getInvoiceDetails(String row[], Map<String, Integer> columnNameToIndexMapper) {
    InvoiceDetail invoiceDetail = null;
    String value = "";
    String docId = row[columnNameToIndexMapper.get("doc_id")];
    try {
      // If primary key is null. SKIP
      if (!docId.isEmpty()) {

        invoiceDetail = new InvoiceDetail();
        // Get rid of .0 appended after every docId in my csv.
        docId = docId.substring(0, docId.lastIndexOf('.'));
        invoiceDetail.setDocId(Long.parseLong(docId));

        // For Business Code
        value = row[columnNameToIndexMapper.get("business_code")];

        if (!value.isEmpty())
          invoiceDetail.setBusinessCode(value);
        else
          invoiceDetail.setBusinessCode(null);

        // For Customer Name
        value = row[columnNameToIndexMapper.get("name_customer")];
        if (!value.isEmpty())
          invoiceDetail.setNameCustomer(value);
        else
          invoiceDetail.setNameCustomer(null);

        // For Customer Number
        value = row[columnNameToIndexMapper.get("cust_number")];
        if (!value.isEmpty())

          invoiceDetail.setCustNumber(value);
        else
          invoiceDetail.setCustNumber(null);

        // For Clear Date.
        value = row[columnNameToIndexMapper.get("clear_date")];
        if (!value.isEmpty()) {
          // Convert String to Timestamp.
          Timestamp clearDateTimestamp = Timestamp.valueOf(value);
          invoiceDetail.setClearDate(clearDateTimestamp);
        } else
          invoiceDetail.setClearDate(null);

        // For Business Year
        value = row[columnNameToIndexMapper.get("buisness_year")];
        if (!value.isEmpty()) {

          value = value.substring(0, value.lastIndexOf('.'));
          invoiceDetail.setBusinessYear(Short.parseShort(value));
        } else
          invoiceDetail.setBusinessYear(null);

        // For Posting Date
        value = row[columnNameToIndexMapper.get("posting_date")];
        if (!value.isEmpty()) {
          Date postingDate = Date.valueOf(value);
          invoiceDetail.setPostingDate(postingDate);
        } else
          invoiceDetail.setPostingDate(null);

        // For Document Create Date
        value = row[columnNameToIndexMapper.get("document_create_date.1")];
        if (!value.isEmpty()) {
          // Convert the date into yyyy-mm-dd format.
          Date date = toDateFormat(value);
          invoiceDetail.setDocumentCreateDate(date);
        } else
          invoiceDetail.setDocumentCreateDate(null);

        // For Due in Date
        value = row[columnNameToIndexMapper.get("due_in_date")];
        if (!value.isEmpty()) {
          // Convert the date into yyyy-mm-dddd format.
          Date date = toDateFormat(value);
          invoiceDetail.setDueInDate(date);
        } else
          invoiceDetail.setDueInDate(null);

        // For Invoice Currency
        value = row[columnNameToIndexMapper.get("invoice_currency")];
        if (!value.isEmpty()) {
          invoiceDetail.setInvoiceCurrency(value);
        } else
          invoiceDetail.setInvoiceCurrency(null);

        // For Document Type
        value = row[columnNameToIndexMapper.get("document type")];
        if (!value.isEmpty()) {
          invoiceDetail.setDocumentType(value);
        } else
          invoiceDetail.setDocumentType(null);

      }

      // For Posting Id
      value = row[columnNameToIndexMapper.get("posting_id")];
      if (!value.isEmpty()) {
        value = value.substring(0, value.lastIndexOf('.'));
        invoiceDetail.setPostingId(Short.parseShort(value));
      } else
        invoiceDetail.setPostingId(null);

      // For Area Business
      value = row[columnNameToIndexMapper.get("area_business")];
      if (!value.isEmpty()) {

        invoiceDetail.setAreaBusiness(value);
      } else
        invoiceDetail.setAreaBusiness(null);

      // For Total Open Amount
      value = row[columnNameToIndexMapper.get("total_open_amount")];
      if (!value.isEmpty()) {

        invoiceDetail.setTotalOpenAmount(Double.parseDouble(value));
      } else
        invoiceDetail.setTotalOpenAmount(null);

      // For Baseline Create Date
      value = row[columnNameToIndexMapper.get("baseline_create_date")];
      if (!value.isEmpty()) {
        // Convert into yyyymmdd format.
        Date date = toDateFormat(value);
        invoiceDetail.setBaselineCreateDate(date);
      } else
        invoiceDetail.setBaselineCreateDate(null);

      // For Customer Payment Terms.
      value = row[columnNameToIndexMapper.get("cust_payment_terms")];
      if (!value.isEmpty()) {

        invoiceDetail.setCustPaymentTerms(value);
      } else
        invoiceDetail.setCustNumber(null);

      // For Invoice - Id
      value = row[columnNameToIndexMapper.get("invoice_id")];
      if (!value.isEmpty()) {
        value = value.substring(0, value.lastIndexOf('.'));
        invoiceDetail.setInvoiceId(Long.parseLong(value));
      } else
        invoiceDetail.setInvoiceId(null);

      // For isOpen
      value = row[columnNameToIndexMapper.get("isOpen")];
      if (!value.isEmpty()) {

        invoiceDetail.setIsOpen(Byte.parseByte(value));
      } else
        invoiceDetail.setIsOpen(null);
    } catch (NullPointerException e) {
      System.out.println(value);
    }
    return invoiceDetail;
  }

  private Date toDateFormat(String dateTime) {
    // Define a SimpleDateFormat with specific pattern.
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    java.util.Date convertedDate = null;
    Date sqlDate = null;
    try {
      // Parse date
      convertedDate = dateFormat.parse(dateTime);
      // Set a new pattern
      SimpleDateFormat sdfnewformat = new SimpleDateFormat("yyyy-MM-dd");
      // Convert into that format.
      String finalDateString = sdfnewformat.format(convertedDate);
      sqlDate = Date.valueOf(finalDateString);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return sqlDate;
  }

  public Flux<ServerSentEvent<InvoiceDetail>> sendDataToConsumer() {
    Flux<ServerSentEvent<InvoiceDetail>> ans = Flux.empty();
    try {
      return fileFlux.map(data -> ServerSentEvent.<InvoiceDetail>builder()
              .data(data)
              .build());
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      return null;
    }
  }
}
