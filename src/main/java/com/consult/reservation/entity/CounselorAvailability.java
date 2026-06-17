package com.consult.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "counselor_availability",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_counselor_availability_counselor_date_slot",
                columnNames = {"counselor_id", "availability_date", "time_slot"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class CounselorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "counselor_id", nullable = false)
    private Long counselorId;

    @Column(name = "availability_date", nullable = false)
    private LocalDate availabilityDate;

    @Column(name = "time_slot", nullable = false, length = 20)
    private String timeSlot;
}
