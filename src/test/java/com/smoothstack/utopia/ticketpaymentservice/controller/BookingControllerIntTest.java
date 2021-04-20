package com.smoothstack.utopia.ticketpaymentservice.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import com.smoothstack.utopia.shared.mailmodels.BaseMailModel;
import com.smoothstack.utopia.shared.model.Airplane;
import com.smoothstack.utopia.shared.model.AirplaneType;
import com.smoothstack.utopia.shared.model.Airport;
import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.BookingAgent;
import com.smoothstack.utopia.shared.model.BookingGuest;
import com.smoothstack.utopia.shared.model.BookingPayment;
import com.smoothstack.utopia.shared.model.BookingUser;
import com.smoothstack.utopia.shared.model.Flight;
import com.smoothstack.utopia.shared.model.Passenger;
import com.smoothstack.utopia.shared.model.Route;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.UserRole;
import com.smoothstack.utopia.shared.service.EmailService;
import com.smoothstack.utopia.ticketpaymentservice.Utils;
import com.smoothstack.utopia.ticketpaymentservice.dao.AirplaneDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.AirplaneTypeDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.AirportDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingAgentDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingGuestDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingPaymentDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingUserDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.FlightDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.PassengerDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.RouteDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.UserDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.UserRoleDao;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateAgentBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateGuestBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreatePassengerDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateUserBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.BookingNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightFullException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentProcessingFailedException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentRefundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.UserNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.service.StripeService;
import com.stripe.exception.CardException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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
class BookingControllerIntTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  private StripeService stripeService;

  @MockBean
  private EmailService emailService;

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

  @Autowired
  private UserDao userDao;

  @Autowired
  private UserRoleDao userRoleDao;

  @Autowired
  private BookingAgentDao bookingAgentDao;

  @Autowired
  private BookingUserDao bookingUserDao;

  private Flight flightLAXtoSFO;
  private Flight flightJFKtoIAH;
  private User user;
  private User agent;

  private Booking createUserOrAgentBooking(
    Set<Flight> flights,
    User u,
    boolean agent
  ) {
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
    if (agent) {
      BookingAgent bookingAgent = new BookingAgent();
      bookingAgent.setBookingId(booking.getId());
      bookingAgent.setAgent(u);
      bookingAgentDao.save(bookingAgent);
      booking.setBookingAgent(bookingAgent);
    } else {
      BookingUser bookingUser = new BookingUser();
      bookingUser.setBookingId(booking.getId());
      bookingUser.setUser(u);
      bookingUserDao.save(bookingUser);
      booking.setBookingUser(bookingUser);
    }
    bookingDao.save(booking);
    return booking;
  }

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

  private void createUserRoles() {
    UserRole userRole = new UserRole();
    userRole.setName("ROLE_USER");
    UserRole agentRole = new UserRole();
    agentRole.setName("ROLE_AGENT");
    UserRole adminRole = new UserRole();
    adminRole.setName("ROLE_ADMIN");
    userRoleDao.saveAll(List.of(userRole, agentRole, adminRole));
  }

  private void createUsers() {
    createUserRoles();
    User user1 = new User();
    user1.setGivenName("Joe");
    user1.setFamilyName("Bob");
    user1.setPassword("supersafe");
    user1.setUserRole(userRoleDao.findUserRoleByName("ROLE_USER").get());
    user1.setPhone("123-456-7890");
    user1.setEmail("example@example.com");
    user1.setActive(true);
    user1.setUsername("joebob123456");
    User user2 = new User();
    user2.setGivenName("Adam");
    user2.setFamilyName("Agent");
    user2.setPassword("incrediblysecure");
    user2.setUserRole(userRoleDao.findUserRoleByName("ROLE_AGENT").get());
    user2.setPhone("111-222-3456");
    user2.setEmail("adam.agent@example.com");
    user2.setActive(true);
    user2.setUsername("adminagent1234");
    userDao.saveAll(List.of(user1, user2));
    user = user1;
    agent = user2;
  }

  @BeforeEach
  public void wipeDb() {
    flightDao.deleteAll();
    passengerDao.deleteAll();
    bookingDao.deleteAll();
    bookingGuestDao.deleteAll();
    bookingUserDao.deleteAll();
    bookingAgentDao.deleteAll();
    bookingPaymentDao.deleteAll();

    airplaneDao.deleteAll();
    airplaneTypeDao.deleteAll();
    routeDao.deleteAll();
    airportDao.deleteAll();
    userDao.deleteAll();
    userRoleDao.deleteAll();
    createUsers();
    createFlights();
  }

  /*
    GET Tests
   */

  @Test
  void canGetAllBookings_whenGetBookings_thenStatus200() throws Exception {
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
  void canGetBooking_whenGetBookingWithId_thenStatus200() throws Exception {
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
  void cannotGetBooking_whenGetBookingWithInvalidId_thenStatus404()
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
  void canGetBooking_whenGetBookingWithConfirmationCode_thenStatus200()
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
  void cannotGetBooking_whenGetBookingWithInvalidConfirmationCode_thenStatus404()
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

  @Test
  void canGetBookings_whenGetBookingsWithValidUserId_thenStatus200()
    throws Exception {
    Booking booking = createUserOrAgentBooking(
      Set.of(flightJFKtoIAH),
      user,
      false
    );
    mvc
      .perform(
        get("/bookings/user/{userId}", user.getId())
          .accept(MediaType.APPLICATION_XML)
      )
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
      .andExpect(
        xpath("List/item[1]/confirmationCode")
          .string(is(booking.getConfirmationCode()))
      );
  }

  @Test
  void cannotGetBookings_whenGetBookingsWithInvalidUserId_thenStatus404()
    throws Exception {
    mvc
      .perform(get("/bookings/user/{userId}", 423L))
      .andExpect(status().isNotFound())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof UserNotFoundException
          )
      );
  }

  /*
    POST Tests
   */
  @Test
  void canCreateBooking_whenPostBookingWithValidData_thenStatus201()
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
    Mockito
      .doNothing()
      .when(emailService)
      .send(Mockito.anyString(), Mockito.any(), Mockito.any());

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
  void cannotCreateBooking_whenPostBookingWithInvalidPayment_thenStatus402()
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
      .thenThrow(CardException.class);
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
  void cannotCreateBooking_whenPostBookingWithInvalidFlight_thenStatus404()
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
  void cannotCreateBooking_whenPostBookingWithFullFlight_thenStatus409()
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

  @Test
  void canCreateUserBooking_whenPostBookingWithValidData_thenStatus201()
    throws Exception {
    CreatePassengerDto passengerDto = new CreatePassengerDto();
    passengerDto.setAddress("Texas");
    passengerDto.setDob(LocalDate.now());
    passengerDto.setGivenName("Joe");
    passengerDto.setFamilyName("Bob");
    passengerDto.setGender("Male");
    CreateUserBookingDto createUserBookingDto = new CreateUserBookingDto();
    createUserBookingDto.setUserId(user.getId());
    createUserBookingDto.setPassengers(List.of(passengerDto));
    createUserBookingDto.setFlightIds(Set.of(flightLAXtoSFO.getId()));
    createUserBookingDto.setStripeToken("STRIPE_TOKEN");
    Mockito
      .when(
        stripeService.chargeCreditCard(Mockito.anyString(), Mockito.anyFloat())
      )
      .thenReturn("STRIPE_ID");
    Mockito
      .doNothing()
      .when(emailService)
      .send(Mockito.anyString(), Mockito.any(), Mockito.any());

    mvc
      .perform(
        post("/bookings/user")
          .content(Utils.asJsonString(createUserBookingDto))
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
            user.getEmail(),
            bookingDao
              .findById(created.getId())
              .get()
              .getBookingUser()
              .getUser()
              .getEmail()
          );
        }
      );
  }

  @Test
  void cannotCreateUserBooking_whenPostBookingWithInvalidUser_thenStatus404()
    throws Exception {
    CreatePassengerDto passengerDto = new CreatePassengerDto();
    passengerDto.setAddress("Texas");
    passengerDto.setDob(LocalDate.now());
    passengerDto.setGivenName("Joe");
    passengerDto.setFamilyName("Bob");
    passengerDto.setGender("Male");
    CreateUserBookingDto createUserBookingDto = new CreateUserBookingDto();
    createUserBookingDto.setUserId(32423L);
    createUserBookingDto.setPassengers(List.of(passengerDto));
    createUserBookingDto.setFlightIds(Set.of(flightLAXtoSFO.getId()));
    createUserBookingDto.setStripeToken("STRIPE_TOKEN");
    mvc
      .perform(
        post("/bookings/user")
          .content(Utils.asJsonString(createUserBookingDto))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof UserNotFoundException
          )
      );
  }

  @Test
  void canCreateAgentBooking_whenPostBookingWithValidData_thenStatus201()
    throws Exception {
    CreatePassengerDto passengerDto = new CreatePassengerDto();
    passengerDto.setAddress("Texas");
    passengerDto.setDob(LocalDate.now());
    passengerDto.setGivenName("Joe");
    passengerDto.setFamilyName("Bob");
    passengerDto.setGender("Male");
    CreateAgentBookingDto createAgentBookingDto = new CreateAgentBookingDto();
    createAgentBookingDto.setAgentId(user.getId());
    createAgentBookingDto.setPassengers(List.of(passengerDto));
    createAgentBookingDto.setFlightIds(Set.of(flightLAXtoSFO.getId()));
    createAgentBookingDto.setStripeToken("STRIPE_TOKEN");
    Mockito
      .when(
        stripeService.chargeCreditCard(Mockito.anyString(), Mockito.anyFloat())
      )
      .thenReturn("STRIPE_ID");
    Mockito
      .doNothing()
      .when(emailService)
      .send(Mockito.anyString(), Mockito.any(), Mockito.any());

    mvc
      .perform(
        post("/bookings/agent")
          .content(Utils.asJsonString(createAgentBookingDto))
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
            user.getEmail(),
            bookingDao
              .findById(created.getId())
              .get()
              .getBookingAgent()
              .getAgent()
              .getEmail()
          );
        }
      );
  }

  @Test
  void cannotCreateAgentBooking_whenPostBookingWithInvalidAgent_thenStatus404()
    throws Exception {
    CreatePassengerDto passengerDto = new CreatePassengerDto();
    passengerDto.setAddress("Texas");
    passengerDto.setDob(LocalDate.now());
    passengerDto.setGivenName("Joe");
    passengerDto.setFamilyName("Bob");
    passengerDto.setGender("Male");
    CreateAgentBookingDto createAgentBookingDto = new CreateAgentBookingDto();
    createAgentBookingDto.setAgentId(4324L);
    createAgentBookingDto.setPassengers(List.of(passengerDto));
    createAgentBookingDto.setFlightIds(Set.of(flightLAXtoSFO.getId()));
    createAgentBookingDto.setStripeToken("STRIPE_TOKEN");
    mvc
      .perform(
        post("/bookings/agent")
          .content(Utils.asJsonString(createAgentBookingDto))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(
        result ->
          Assertions.assertTrue(
            result.getResolvedException() instanceof UserNotFoundException
          )
      );
  }

  /*
    DELETE Tests
   */
  @Test
  void canCancelBooking_whenDeleteBookingWithValidId_thenStatus204()
    throws Exception {
    Booking booking = createGuestBooking(Set.of(flightLAXtoSFO));
    Mockito.doNothing().when(stripeService).refundCharge(Mockito.anyString());
    mvc
      .perform(delete("/bookings/{id}", booking.getId()))
      .andExpect(status().isNoContent())
      .andExpect(
        result -> {
          Booking cancelledBooking = bookingDao.findById(booking.getId()).get();
          Assertions.assertFalse(cancelledBooking.getIsActive());
          Assertions.assertTrue(
            cancelledBooking.getBookingPayment().getRefunded()
          );
        }
      );
  }

  @Test
  void cannotCancelBooking_whenDeleteBookingWithInvalidId_thenStatus404()
    throws Exception {
    mvc
      .perform(delete("/bookings/{id}", 234234L))
      .andExpect(status().isNotFound())
      .andExpect(
        result -> {
          Assertions.assertTrue(
            result.getResolvedException() instanceof BookingNotFoundException
          );
        }
      );
  }

  @Test
  void cannotCancelBooking_whenDeleteBookingWithRefundException_thenStatus402()
    throws Exception {
    Booking booking = createGuestBooking(Set.of(flightLAXtoSFO));
    Mockito
      .doThrow(new PaymentRefundException())
      .when(stripeService)
      .refundCharge(Mockito.anyString());
    mvc
      .perform(delete("/bookings/{id}", booking.getId()))
      .andExpect(status().isPaymentRequired())
      .andExpect(
        result -> {
          Assertions.assertTrue(
            result.getResolvedException() instanceof PaymentRefundException
          );
          Booking cancelledBooking = bookingDao.findById(booking.getId()).get();
          Assertions.assertTrue(cancelledBooking.getIsActive());
          Assertions.assertFalse(
            cancelledBooking.getBookingPayment().getRefunded()
          );
        }
      );
  }
}
