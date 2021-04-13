package com.smoothstack.utopia.ticketpaymentservice.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.BookingPayment;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingPaymentDao;
import com.smoothstack.utopia.ticketpaymentservice.dto.PaymentInfoDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.service.StripeService;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Rob Maes
 * Apr 05 2021
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
class PaymentControllerIntTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  private StripeService stripeService;

  @Autowired
  BookingPaymentDao bookingPaymentDao;

  @Autowired
  BookingDao bookingDao;

  private BookingPayment createBookingPayment() {
    Booking booking = new Booking();
    booking.setConfirmationCode(UUID.randomUUID().toString());
    booking.setIsActive(true);
    bookingDao.save(booking);
    BookingPayment bookingPayment = new BookingPayment();
    bookingPayment.setRefunded(false);
    bookingPayment.setStripeId("STRIPE_ID");
    bookingPayment.setBookingId(booking.getId());
    bookingPaymentDao.save(bookingPayment);
    return bookingPayment;
  }

  @BeforeEach
  public void wipeDb() {
    bookingPaymentDao.deleteAll();
    bookingDao.deleteAll();
  }

  /*
    GET Tests
   */
  @Test
  void canGetPayment_whenGetPaymentWithValidId_thenStatus200()
    throws Exception {
    BookingPayment payment = createBookingPayment();
    PaymentInfoDto returnInfo = new PaymentInfoDto();
    returnInfo.setLastFour("1234");
    returnInfo.setCurrency("usd");
    returnInfo.setCreated(1617389017L);
    returnInfo.setAmount(21650L);
    returnInfo.setCardBrand("Visa");
    Mockito
      .when(stripeService.getPaymentInfo(Mockito.anyString()))
      .thenReturn(returnInfo);
    mvc
      .perform(
        get("/payments/{stripeId}", payment.getStripeId())
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(jsonPath("$.lastFour", is("1234")));
  }

  @Test
  void cannotGetPayment_whenGetPaymentWithInvalidId_thenStatus404()
    throws Exception {
    Mockito
      .when(stripeService.getPaymentInfo(Mockito.anyString()))
      .thenThrow(PaymentNotFoundException.class);
    mvc
      .perform(
        get("/payments/{stripeId}", "123").accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof PaymentNotFoundException
          )
      );
  }
}
