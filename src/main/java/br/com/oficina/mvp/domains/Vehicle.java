package br.com.oficina.mvp.domains;

import br.com.oficina.mvp.domains.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, unique = true)
    private String plate;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    protected Vehicle() {}

    public Vehicle(Customer customer, String plate, String brand, String model, Integer year) {
        this.customer = customer;
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.year = year;
    }

    public void update(Customer customer, String plate, String brand, String model, Integer year) {
        if (customer != null) this.customer = customer;
        if (plate != null) this.plate = plate;
        if (brand != null) this.brand = brand;
        if (model != null) this.model = model;
        if (year != null) this.year = year;
    }

    public Customer getCustomer() { return customer; }
    public String getPlate() { return plate; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
}
