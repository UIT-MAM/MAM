package com.se114p12.backend.entities.variation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.se114p12.backend.entities.BaseEntity;
import com.se114p12.backend.entities.cart.CartItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "variation_options")
public class VariationOption extends BaseEntity {
  @Column(nullable = false, length = 255, name = "option_value")
  @NotNull
  private String value;

  @NotNull private Double additionalPrice;

  @ManyToOne
  @JoinColumn(name = "variation_id", nullable = true)
  @JsonBackReference
  private Variation variation;

  @ManyToMany(mappedBy = "variationOptions")
  private List<CartItem> cartItems = new ArrayList<>();
}
