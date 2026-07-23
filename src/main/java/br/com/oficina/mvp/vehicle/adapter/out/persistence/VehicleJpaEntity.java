package br.com.oficina.mvp.vehicle.adapter.out.persistence;

import br.com.oficina.mvp.customer.adapter.out.persistence.CustomerJpaEntity;
import br.com.oficina.mvp.shared.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class VehicleJpaEntity extends BaseJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @Column(nullable = false, unique = true)
    private String plate;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "manufacturing_year", nullable = false)
    private Integer manufacturingYear;

    protected VehicleJpaEntity() {}

    VehicleJpaEntity(CustomerJpaEntity customer, String plate, String brand, String model, Integer manufacturingYear) {
        this.customer = customer;
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
    }

    CustomerJpaEntity getCustomer() { return customer; }
    String getPlate() { return plate; }
    String getBrand() { return brand; }
    String getModel() { return model; }
    Integer getManufacturingYear() { return manufacturingYear; }

    void setCustomer(CustomerJpaEntity customer) { this.customer = customer; }
    void setPlate(String plate) { this.plate = plate; }
    void setBrand(String brand) { this.brand = brand; }
    void setModel(String model) { this.model = model; }
    void setManufacturingYear(Integer manufacturingYear) { this.manufacturingYear = manufacturingYear; }
}
