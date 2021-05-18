package splitter.services;

import splitter.entities.Payment;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {
    void add(Payment payment);

    List<Payment> balance(LocalDate date);
}
