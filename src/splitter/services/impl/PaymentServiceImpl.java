package splitter.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import splitter.PaymentSummary;
import splitter.entities.Payment;
import splitter.repos.PaymentRepository;
import splitter.services.PaymentService;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentRepository paymentRepository;

    @Override
    public void add(Payment payment) {
        paymentRepository.save(payment);
    }

    @Override
    public List<PaymentSummary> balance(LocalDate date) {
        return paymentRepository.balance(date);
    }

    @Override
    public void writeOff(LocalDate date) {
        paymentRepository.deleteByDateLessThanEqual(date);
    }
}
