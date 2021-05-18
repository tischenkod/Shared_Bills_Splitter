package splitter.repos;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import splitter.PaymentSummary;
import splitter.entities.Payment;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends PagingAndSortingRepository<Payment, Integer> {

    @Query("SELECT p.sender, p.receiver, SUM(p.amount) as amount FROM Payment p WHERE date <= :date GROUP BY sender, receiver ORDER BY sender, receiver")
    List<Payment> balance(@Param("date") LocalDate date);

//    List<Payment> findAll();
}
