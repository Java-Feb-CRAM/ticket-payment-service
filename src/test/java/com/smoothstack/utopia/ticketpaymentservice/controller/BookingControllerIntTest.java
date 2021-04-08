package com.smoothstack.utopia.ticketpaymentservice.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import com.netflix.discovery.converters.Auto;
import com.smoothstack.utopia.shared.model.Airplane;
import com.smoothstack.utopia.shared.model.AirplaneType;
import com.smoothstack.utopia.shared.model.Airport;
import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.BookingGuest;
import com.smoothstack.utopia.shared.model.BookingPayment;
import com.smoothstack.utopia.shared.model.Flight;
import com.smoothstack.utopia.shared.model.Passenger;
import com.smoothstack.utopia.shared.model.Route;
import com.smoothstack.utopia.ticketpaymentservice.Utils;
import com.smoothstack.utopia.ticketpaymentservice.dao.AirplaneDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.AirplaneTypeDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.AirportDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingGuestDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingPaymentDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.FlightDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.PassengerDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.RouteDao;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateGuestBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreatePassengerDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.BookingNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightFullException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentProcessingFailedException;
import com.smoothstack.utopia.ticketpaymentservice.service.StripeService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.Matchers;
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
public class BookingControllerIntTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  private StripeService stripeService;

  @Autowired
  private BookingGuestDao bookingGuestDao;

  @Autowired
  private BookingPaymentDao bookingPaymentDao;

  @Autowired
  private PassengerDao passengerDao;

  @Autowired
  private BookingDao bookingDao;

  @Autowired
  private FlightDao flightDao;

  @Autowired
  private AirplaneDao airplaneDao;

  @Autowired
  private AirplaneTypeDao airplaneTypeDao;

  @Autowired
  private AirportDao airportDao;

  @Autowired
  private RouteDao routeDao;

  private Flight flightLAXtoSFO;
  private Flight flightJFKtoIAH;

  private Booking createGuestBooking(Set<Flight> flights) throws Exception {
    Booking booking = new Booking();
    booking.setFlights(flights);
    booking.setConfirmationCode(UUID.randomUUID().toString());
    booking.setIsActive(true);
    bookingDao.save(booking);
    Passenger passenger = new Passenger();
    passenger.setAddress("California");
    passenger.setDob(LocalDate.now());
    passenger.setGender("Male");
    passenger.setGivenName("John");
    passenger.setFamilyName("Smith");
    passenger.setBooking(booking);
    passengerDao.save(passenger);
    booking.setPassengers(Set.of(passenger));
    bookingDao.save(booking);
    BookingPayment payment = new BookingPayment();
    payment.setBookingId(booking.getId());
    payment.setStripeId("XYZ");
    payment.setRefunded(false);
    bookingPaymentDao.save(payment);
    booking.setBookingPayment(payment);
    bookingDao.save(booking);
    BookingGuest guest = new BookingGuest();
    guest.setBookingId(booking.getId());
    guest.setContactPhone("123-456-7890");
    guest.setContactEmail("johnsmith@example.com");
    bookingGuestDao.save(guest);
    booking.setBookingGuest(guest);
    bookingDao.save(booking);
    return booking;
  }

  private void createFlights() {
    Airport airportLAX = new Airport();
    airportLAX.setIataId("LAX");
    airportLAX.setCity("Los Angeles");
    airportDao.save(airportLAX);
    Airport airportSFO = new Airport();
    airportSFO.setIataId("SFO");
    airportSFO.setCity("San Francisco");
    airportDao.save(airportSFO);
    Airport airportJFK = new Airport();
    airportJFK.setIataId("JFK");
    airportJFK.setCity("New York City");
    airportDao.save(airportJFK);
    Airport airportIAH = new Airport();
    airportIAH.setIataId("IAH");
    airportIAH.setCity("Houston");
    airportDao.save(airportIAH);
    Route routeLAXtoSFO = new Route();
    routeLAXtoSFO.setOriginAirport(airportLAX);
    routeLAXtoSFO.setDestinationAirport(airportSFO);
    routeDao.save(routeLAXtoSFO);
    Route routeJFKtoIAH = new Route();
    routeJFKtoIAH.setOriginAirport(airportJFK);
    routeJFKtoIAH.setDestinationAirport(airportIAH);
    routeDao.save(routeJFKtoIAH);
    AirplaneType airplaneType = new AirplaneType();
    airplaneType.setMaxCapacity(5);
    airplaneTypeDao.save(airplaneType);
    Airplane airplane = new Airplane();
    airplane.setAirplaneType(airplaneType);
    airplaneDao.save(airplane);
    flightLAXtoSFO = new Flight();
    flightLAXtoSFO.setDepartureTime(Instant.now());
    flightLAXtoSFO.setReservedSeats(1);
    flightLAXtoSFO.setRoute(routeLAXtoSFO);
    flightLAXtoSFO.setAirplane(airplane);
    flightLAXtoSFO.setSeatPrice(20.0f);
    flightDao.save(flightLAXtoSFO);
    flightJFKtoIAH = new Flight();
    flightJFKtoIAH.setDepartureTime(Instant.now());
    flightJFKtoIAH.setReservedSeats(2);
    flightJFKtoIAH.setRoute(routeJFKtoIAH);
    flightJFKtoIAH.setAirplane(airplane);
    flightJFKtoIAH.setSeatPrice(15.0f);
    flightDao.save(flightJFKtoIAH);
  }

  @BeforeEach
  public void wipeDb() {
    flightDao.deleteAll();
    passengerDao.deleteAll();
    bookingDao.deleteAll();
    bookingGuestDao.deleteAll();
    bookingPaymentDao.deleteAll();

    airplaneDao.deleteAll();
    airplaneTypeDao.deleteAll();
    routeDao.deleteAll();
    airportDao.deleteAll();
    createFlights();
  }

  /*
    GET Tests
   */

  @Test
  public void canGetAllBookings_whenGetBookings_thenStatus200()
    throws Exception {
    Booking booking = createGuestBooking(Set.of(flightJFKtoIAH));
    mvc
      .perform(get("/bookings").accept(MediaType.APPLICATION_XML))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
      .andExpect(
        xpath("List/item[1]/confirmationCode")
          .string(is(booking.getConfirmationCode()))
      );
  }

  @Test
  public void canGetBooking_whenGetBookingWithId_thenStatus200()
    throws Exception {
    Booking booking = createGuestBooking(Set.of(flightLAXtoSFO));
    mvc
      .perform(get("/bookings/{id}", booking.getId()))
      .andExpect(status().isOk())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(
        jsonPath("$.confirmationCode", is(booking.getConfirmationCode()))
      );
  }

  @Test
  public void cannotGetBooking_whenGetBookingWithInvalidId_thenStatus404()
    throws Exception {
    mvc
      .perform(get("/bookings/{id}", 2))
      .andExpect(status().isNotFound())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof BookingNotFoundException
          )
      );
  }

  @Test
  public void canGetBooking_whenGetBookingWithConfirmationCode_thenStatus200()
    throws Exception {
    Booking booking = createGuestBooking(Set.of(flightLAXtoSFO));
    mvc
      .perform(
        get(
          "/bookings/confirmation/{confirmationCode}",
          booking.getConfirmationCode()
        )
      )
      .andExpect(status().isOk())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(
        jsonPath("$.confirmationCode", is(booking.getConfirmationCode()))
      );
  }

  @Test
  public void cannotGetBooking_whenGetBookingWithInvalidConfirmationCode_thenStatus404()
    throws Exception {
    mvc
      .perform(
        get(
          "/bookings/confirmation/{confirmationCode}",
          UUID.randomUUID().toString()
        )
      )
      .andExpect(status().isNotFound())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof BookingNotFoundException
          )
      );
  }

  /*
    POST Tests
   */
  @Test
  public void canCreateBooking_whenPostBookingWithValidData_thenStatus201()
    throws Exception {
    CreatePassengerDto passengerDto = new CreatePassengerDto();
    passengerDto.setAddress("Texas");
    passengerDto.setDob(LocalDate.now());
    passengerDto.setGivenName("Joe");
    passengerDto.setFamilyName("Bob");
    passengerDto.setGender("Male");
    CreateGuestBookingDto createGuestBookingDto = new CreateGuestBookingDto();
    createGuestBookingDto.setGuestEmail("joebob@example.com");
    createGuestBookingDto.setGuestPhone("123-456-7890");
    createGuestBookingDto.setFlightIds(Set.of(flightLAXtoSFO.getId()));
    createGuestBookingDto.setStripeToken("STRIPE_TOKEN");
    createGuestBookingDto.setPassengers(List.of(passengerDto));
    Mockito
      .when(
        stripeService.chargeCreditCard(Mockito.anyString(), Mockito.anyFloat())
      )
      .thenReturn("STRIPE_ID");
    mvc
      .perform(
        post("/bookings/guest")
          .content(Utils.asJsonString(createGuestBookingDto))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(jsonPath("$.passengers[0].givenName", is("Joe")))
      .andExpect(
        result -> {
          Booking created = Utils
            .getMapper()
            .readValue(
              result.getResponse().getContentAsString(),
              Booking.class
            );
          Assertions.assertEquals(
            "joebob@example.com",
            bookingDao
              .findById(created.getId())
              .get()
              .getBookingGuest()
              .getContactEmail()
          );
          Assertions.assertEquals(
            "STRIPE_ID",
            bookingDao
              .findById(created.getId())
              .get()
              .getBookingPayment()
              .getStripeId()
          );
        }
      );
  }

  @Test
  public void cannotCreateBooking_whenPostBookingWithInvalidPayment_thenStatus402()
    throws Exception {
    CreatePassengerDto passengerDto = new CreatePassengerDto();
    passengerDto.setAddress("Texas");
    passengerDto.setDob(LocalDate.now());
    passengerDto.setGivenName("Joe");
    passengerDto.setFamilyName("Bob");
    passengerDto.setGender("Male");
    CreateGuestBookingDto createGuestBookingDto = new CreateGuestBookingDto();
    createGuestBookingDto.setGuestEmail("joebob@example.com");
    createGuestBookingDto.setGuestPhone("123-456-7890");
    createGuestBookingDto.setFlightIds(Set.of(flightLAXtoSFO.getId()));
    createGuestBookingDto.setStripeToken("STRIPE_TOKEN");
    createGuestBookingDto.setPassengers(List.of(passengerDto));
    Mockito
      .when(
        stripeService.chargeCreditCard(Mockito.anyString(), Mockito.anyFloat())
      )
      .thenThrow(Exception.class);
    mvc
      .perform(
        post("/bookings/guest")
          .content(Utils.asJsonString(createGuestBookingDto))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isPaymentRequired())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof PaymentProcessingFailedException
          )
      );
  }

  @Test
  public void cannotCreateBooking_whenPostBookingWithInvalidFlight_thenStatus404()
    throws Exception {
    CreatePassengerDto passengerDto = new CreatePassengerDto();
    passengerDto.setAddress("Texas");
    passengerDto.setDob(LocalDate.now());
    passengerDto.setGivenName("Joe");
    passengerDto.setFamilyName("Bob");
    passengerDto.setGender("Male");
    CreateGuestBookingDto createGuestBookingDto = new CreateGuestBookingDto();
    createGuestBookingDto.setGuestEmail("joebob@example.com");
    createGuestBookingDto.setGuestPhone("123-456-7890");
    createGuestBookingDto.setFlightIds(Set.of(1234L));
    createGuestBookingDto.setStripeToken("STRIPE_TOKEN");
    createGuestBookingDto.setPassengers(List.of(passengerDto));
    Mockito
      .when(
        stripeService.chargeCreditCard(Mockito.anyString(), Mockito.anyFloat())
      )
      .thenReturn("STRIPE_ID");
    mvc
      .perform(
        post("/bookings/guest")
          .content(Utils.asJsonString(createGuestBookingDto))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof FlightNotFoundException
          )
      );
  }

  @Test
  public void cannotCreateBooking_whenPostBookingWithFullFlight_thenStatus409()
    throws Exception {
    CreatePassengerDto passengerDto = new CreatePassengerDto();
    passengerDto.setAddress("Texas");
    passengerDto.setDob(LocalDate.now());
    passengerDto.setGivenName("Joe");
    passengerDto.setFamilyName("Bob");
    passengerDto.setGender("Male");
    CreateGuestBookingDto createGuestBookingDto = new CreateGuestBookingDto();
    createGuestBookingDto.setGuestEmail("joebob@example.com");
    createGuestBookingDto.setGuestPhone("123-456-7890");
    createGuestBookingDto.setFlightIds(Set.of(flightJFKtoIAH.getId()));
    createGuestBookingDto.setStripeToken("STRIPE_TOKEN");
    createGuestBookingDto.setPassengers(
      List.of(passengerDto, passengerDto, passengerDto, passengerDto)
    );
    Mockito
      .when(
        stripeService.chargeCreditCard(Mockito.anyString(), Mockito.anyFloat())
      )
      .thenReturn("STRIPE_ID");
    mvc
      .perform(
        post("/bookings/guest")
          .content(Utils.asJsonString(createGuestBookingDto))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isConflict())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof FlightFullException
          )
      );
  }
}
