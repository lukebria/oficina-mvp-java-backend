package br.com.oficina.mvp.vehicle.domain;

import br.com.oficina.mvp.customer.domain.Customer;
import br.com.oficina.mvp.shared.domain.BaseDomain;

import java.time.OffsetDateTime;

public class Vehicle extends BaseDomain {
    private Customer customer;
    private String plate;
    private String brand;
    private String model;
    private Integer manufacturingYear;

    public Vehicle(Customer customer, String plate, String brand, String model, Integer manufacturingYear) {
        this.customer = customer;
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
    }

    public Vehicle(Long id, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                    Customer customer, String plate, String brand, String model, Integer manufacturingYear) {
        super(id, createdAt, updatedAt);
        this.customer = customer;
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
    }

    public void update(Customer customer, String plate, String brand, String model, Integer year) {
        if (customer != null) this.customer = customer;
        if (plate != null) this.plate = plate;
        if (brand != null) this.brand = brand;
        if (model != null) this.model = model;
        if (year != null) this.manufacturingYear = year;
    }

    public Customer getCustomer() { return customer; }
    public String getPlate() { return plate; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Integer getManufacturingYear() { return manufacturingYear; }
}
