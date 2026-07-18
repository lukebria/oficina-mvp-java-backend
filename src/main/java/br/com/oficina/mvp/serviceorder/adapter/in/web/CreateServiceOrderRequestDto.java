package br.com.oficina.mvp.serviceorder.adapter.in.web;

import br.com.oficina.mvp.serviceorder.application.port.in.CreateServiceOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateServiceOrderRequestDto(
        @NotBlank @Size(min = 11, max = 18) String customerDocument,
        @Valid @NotNull CustomerData customer,
        @Valid @NotNull VehicleData vehicle,
        String customerNotes,
        @Valid @NotEmpty List<ServiceItemData> services,
        @Valid List<PartItemData> parts
) {
    public record CustomerData(
            @NotBlank @Size(min = 2) String name,
            @Email String email,
            @Size(min = 8) String phone
    ) {}

    public record VehicleData(
            @NotBlank @Size(min = 7, max = 8) String plate,
            @NotBlank @Size(min = 2) String brand,
            @NotBlank String model,
            @NotNull @Min(1900) Integer manufacturingYear
    ) {}

    public record ServiceItemData(
            @NotNull Long serviceItemId,
            @Positive Integer quantity
    ) {}

    public record PartItemData(
            @NotNull Long partId,
            @NotNull @Positive Integer quantity
    ) {}

    public CreateServiceOrderCommand toCommand() {
        return new CreateServiceOrderCommand(
                customerDocument(),
                new CreateServiceOrderCommand.CustomerData(customer().name(), customer().email(), customer().phone()),
                new CreateServiceOrderCommand.VehicleData(vehicle().plate(), vehicle().brand(), vehicle().model(), vehicle().manufacturingYear()),
                customerNotes(),
                services().stream().map(s -> new CreateServiceOrderCommand.ServiceItemData(s.serviceItemId(), s.quantity())).toList(),
                parts() == null ? null : parts().stream().map(p -> new CreateServiceOrderCommand.PartItemData(p.partId(), p.quantity())).toList()
        );
    }
}
