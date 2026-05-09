package com.barinventory.entities;

import java.util.List;
import java.util.stream.Collectors;

import com.barinventory.enums.GlobalRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor   // ✅ REQUIRED for JPA
public class BarUser {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String password;

    // ✅ bar mapping (keep simple for now)
    private Long barId;
    
    @Enumerated(EnumType.STRING)
    private GlobalRole role;

    @OneToMany(mappedBy = "user")
    private List<UserBarAccess> barAccesses;

    private Boolean active = true;

    public List<UserBarAccess> getActiveBarAccesses() {
        if (barAccesses == null) {
            return List.of();
        }
        return barAccesses.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .collect(Collectors.toList());
    }
}
