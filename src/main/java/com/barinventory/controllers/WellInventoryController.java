package com.barinventory.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.barinventory.config.SecurityUtils;
import com.barinventory.dtos.WellClosingRequest;
import com.barinventory.entities.InventoryStatus;
import com.barinventory.entities.Well;
import com.barinventory.entities.WellInventory;
import com.barinventory.services.BarPriceService;
import com.barinventory.services.WellInventoryService;
import com.barinventory.services.WellService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/well")
public class WellInventoryController {

	private final WellService wellService;
	private final WellInventoryService wellInventoryService;
	private final BarPriceService barPriceService;

	/*
	 * ----------------------------------------- SELECT WELL PAGE
	 * -----------------------------------------
	 */
	@GetMapping("/select/{sessionId}")
	public String selectWellPage(@PathVariable Long sessionId, Model model) {

		Long barId = SecurityUtils.getBarId();

		List<Well> wells = wellService.getWellsByBar(barId);

		Map<Long, InventoryStatus> wellStatusMap = wellInventoryService.getWellStatuses(barId, sessionId);

		boolean completed = wellInventoryService.isSessionCompleted(barId, sessionId);

		int progress = wellInventoryService.getSessionProgress(barId, sessionId);

		model.addAttribute("wells", wells);

		model.addAttribute("sessionId", sessionId);

		model.addAttribute("barId", barId);

		model.addAttribute("wellStatusMap", wellStatusMap);

		model.addAttribute("sessionCompleted", completed);

		model.addAttribute("progress", progress);

		return "well/well-selection";
	}

	/*
	 * ----------------------------------------- START WELL INVENTORY (AUTO)
	 * -----------------------------------------
	 */
	@GetMapping("/start/{sessionId}")
	public String startWellInventory(@PathVariable Long sessionId) {
		Long barId = SecurityUtils.getBarId();

		if (wellInventoryService.isSessionCompleted(barId, sessionId)) {
			return "redirect:/well/select/" + sessionId;
		}

		Long nextWellId = wellInventoryService.getNextPendingWell(barId, sessionId);
		if (nextWellId == null) {
			return "redirect:/well/select/" + sessionId;
		}

		List<WellInventory> existing = wellInventoryService.getWellInventory(barId, sessionId, nextWellId);
		if (existing.isEmpty()) {
			wellInventoryService.initializeWellInventory(barId, sessionId, nextWellId);
		}

		return "redirect:/well/" + sessionId + "/" + nextWellId;
	}

	/*
	 * ----------------------------------------- INITIALIZE WELL
	 * -----------------------------------------
	 */
	@PostMapping("/initialize/{sessionId}/{wellId}")
	public String initializeWell(

			@PathVariable Long sessionId,

			@PathVariable Long wellId) {

		Long barId = SecurityUtils.getBarId();

		if (wellInventoryService.isSessionCompleted(barId, sessionId)) {

			return "redirect:/well/select/" + sessionId;
		}

		List<WellInventory> existing = wellInventoryService.getWellInventory(barId, sessionId, wellId);

		boolean wellCompleted = !existing.isEmpty()
				&& existing.stream().allMatch(i -> i.getStatus() == InventoryStatus.COMPLETED);

		if (wellCompleted) {

			return "redirect:/well/select/" + sessionId;
		}

		wellInventoryService.initializeWellInventory(barId, sessionId, wellId);

		return "redirect:/well/" + sessionId + "/" + wellId;
	}

	/*
	 * ----------------------------------------- WELL INVENTORY PAGE
	 * -----------------------------------------
	 */
	@GetMapping("/{sessionId}/{wellId}")
	public String wellInventoryPage(

			@PathVariable Long sessionId,

			@PathVariable Long wellId,

			Model model) {

		Long barId = SecurityUtils.getBarId();

		if (wellInventoryService.isSessionCompleted(barId, sessionId)) {

			return "redirect:/well/select/" + sessionId;
		}

		List<WellInventory> existing = wellInventoryService.getWellInventory(barId, sessionId, wellId);

		boolean completed = !existing.isEmpty()
				&& existing.stream().allMatch(i -> i.getStatus() == InventoryStatus.COMPLETED);

		if (completed) {

			return "redirect:/well/select/" + sessionId;
		}

		List<WellInventory> inventory = wellInventoryService.getWellInventory(barId, sessionId, wellId);

		Map<Long, Double> priceMap = barPriceService.getPriceMap(barId);

		int progress = wellInventoryService.getSessionProgress(barId, sessionId);

		model.addAttribute("progress", progress);

		model.addAttribute("wellInventory", inventory);

		model.addAttribute("sessionId", sessionId);

		model.addAttribute("wellId", wellId);

		model.addAttribute("barId", barId);

		model.addAttribute("priceMap", priceMap);

		return "well/well-inventory";
	}

	/*
	 * ----------------------------------------- UPDATE WELL CLOSING
	 * -----------------------------------------
	 */
	@PostMapping("/closing/{sessionId}/{wellId}")
	public String updateClosing(

			@PathVariable Long sessionId,

			@PathVariable Long wellId,

			@RequestParam List<Long> brandSizeId,

			@RequestParam List<Integer> closingStock) {

		Long barId = SecurityUtils.getBarId();

		List<WellClosingRequest> requests = new ArrayList<>();

		for (int i = 0; i < brandSizeId.size(); i++) {

			WellClosingRequest req = new WellClosingRequest();

			req.setBrandSizeId(brandSizeId.get(i));

			req.setClosingStock(closingStock.get(i));

			requests.add(req);
		}

		wellInventoryService.updateWellClosing(barId, sessionId, wellId, requests);

		Long nextWellId = wellInventoryService.getNextPendingWell(barId, sessionId);

		if (nextWellId != null) {

			List<WellInventory> nextInv = wellInventoryService.getWellInventory(barId, sessionId, nextWellId);

			if (nextInv.isEmpty()) {

				wellInventoryService.initializeWellInventory(barId, sessionId, nextWellId);
			}

			return "redirect:/well/" + sessionId + "/" + nextWellId;
		}

		return "redirect:/well/select/" + sessionId;
	}
}
