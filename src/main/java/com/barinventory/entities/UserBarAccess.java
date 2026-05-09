package com.barinventory.entities;

import java.time.LocalDateTime;

import com.barinventory.enums.BarRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_bar_access", uniqueConstraints = {
		@UniqueConstraint(name = "uk_user_bar_access", columnNames = { "user_id", "bar_id" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBarAccess {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ─────────────────────────────────────────────
	// USER
	// ─────────────────────────────────────────────
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private BarUser user;

	// ─────────────────────────────────────────────
	// BAR
	// ─────────────────────────────────────────────
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bar_id", nullable = false)
	private Bar bar;

	// ─────────────────────────────────────────────
	// ROLE INSIDE THIS BAR
	// ─────────────────────────────────────────────
	@Enumerated(EnumType.STRING)
	@Column(name = "bar_role", nullable = false, length = 30)
	private BarRole barRole;

	/*
	 * Examples: BAR_OWNER BAR_MANAGER BAR_STAFF
	 */

	// ─────────────────────────────────────────────
	// ACCESS STATUS
	// ─────────────────────────────────────────────
	@Builder.Default
	@Column(nullable = false)
	private Boolean active = true;

	// ─────────────────────────────────────────────
	// AUDIT FIELDS
	// ─────────────────────────────────────────────
	@Column(name = "created_at", nullable = false, updatable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Who granted access
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "granted_by")
	private BarUser grantedBy;

	// ─────────────────────────────────────────────
	// HELPER METHODS
	// ─────────────────────────────────────────────

	public boolean isOwner() {
		return barRole == BarRole.BAR_OWNER;
	}

	public boolean isManager() {
		return barRole == BarRole.BAR_MANAGER;
	}

	public boolean isStaff() {
		return barRole == BarRole.BAR_STAFF;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}