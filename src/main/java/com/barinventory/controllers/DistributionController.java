package com.barinventory.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.barinventory.config.SecurityUtils;
import com.barinventory.dtos.DistributionRequest;
import com.barinventory.dtos.DistributionRequestWrapper;
import com.barinventory.entities.Brand;
import com.barinventory.entities.BrandSize;
import com.barinventory.entities.Distribution;
import com.barinventory.entities.StockroomInventory;
import com.barinventory.entities.Well;
import com.barinventory.services.BrandService;
import com.barinventory.services.DistributionService;
import com.barinventory.services.StockroomInventoryService;
import com.barinventory.services.WellService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/distribution")
public class DistributionController {

    private final DistributionService distributionService;
    private final BrandService brandService;
    private final WellService wellService;
    private final StockroomInventoryService stockroomService;

    /*
     -----------------------------------------
     CREATE DISTRIBUTION PAGE
     -----------------------------------------
    */
    @GetMapping("/create-page/{sessionId}")
    public String createDistributionPage(
            @PathVariable Long sessionId,
            Model model
    ) {

        model.addAttribute("sessionId", sessionId);

        return "distribution/distribution-create";
    }

    /*
     -----------------------------------------
     CREATE DISTRIBUTION
     -----------------------------------------
    */
    @PostMapping("/create/{sessionId}")
    public String createDistribution(
            @PathVariable Long sessionId
    ) {

        Long barId = SecurityUtils.getBarId();

        Distribution distribution =
                distributionService.createDistribution(
                        barId,
                        sessionId
                );

        return "redirect:/distribution/allocation/"
                + distribution.getDistributionId();
    }

    /*
     -----------------------------------------
     ALLOCATION PAGE
     -----------------------------------------
    */
    @GetMapping("/allocation/{distributionId}")
    public String allocationPage(@PathVariable Long distributionId, Model model) {

        Long barId = SecurityUtils.getBarId();
        populateAllocationModel(barId, distributionId, null, model);

        return "distribution/distribution-allocation";
    }
    /*
     * 
     -----------------------------------------
     DISTRIBUTE STOCK
     -----------------------------------------
    */
    @PostMapping("/allocate/{distributionId}")
    public String distribute(
            @PathVariable Long distributionId,
            @ModelAttribute DistributionRequestWrapper wrapper,
            Model model
    ) {

        Long barId = SecurityUtils.getBarId();

        try {

            distributionService.distributeStock(
                    distributionId,
                    wrapper.getRequests()
            );

            Long sessionId =
                    distributionService
                            .getSessionIdByDistribution(
                                    distributionId
                            );

            return "redirect:/well/start/" + sessionId;

        } catch (RuntimeException ex) {

            ex.printStackTrace();

            populateAllocationModel(barId, distributionId, wrapper, model);
            model.addAttribute("error", ex.getMessage());

            return "distribution/distribution-allocation";
        }
    }

    private void populateAllocationModel(
            Long barId,
            Long distributionId,
            DistributionRequestWrapper existingWrapper,
            Model model
    ) {

        List<Brand> brands = brandService.getBrandsByBar(barId);

        List<Well> wells = wellService.getWellsByBar(barId);
        if (wells == null || wells.isEmpty()) {
            wells = wellService.getAllWells();
        }

        Map<Long, Integer> stockMap = stockroomService.getSaleStockMap(distributionId);

        DistributionRequestWrapper wrapper = existingWrapper;
        if (wrapper == null || wrapper.getRequests() == null || wrapper.getRequests().isEmpty()) {
            List<DistributionRequest> requests = new ArrayList<>();
            for (Brand brand : brands) {
                if (brand.getBrandSizes() == null || brand.getBrandSizes().isEmpty()) {
                    continue;
                }
                for (BrandSize brandSize : brand.getBrandSizes()) {
                    for (Well well : wells) {
                        DistributionRequest req = new DistributionRequest();
                        req.setBrandId(brand.getBrandId());
                        req.setBrandSizeId(brandSize.getBrandSizeId());
                        req.setWellId(well.getWellId());
                        req.setDistributedQty(0);
                        requests.add(req);
                    }
                }
            }
            wrapper = new DistributionRequestWrapper();
            wrapper.setRequests(requests);
        }

        Map<String, Integer> indexMap = new HashMap<>();
        int idx = 0;
        for (Brand brand : brands) {
            if (brand.getBrandSizes() == null || brand.getBrandSizes().isEmpty()) {
                continue;
            }
            for (BrandSize brandSize : brand.getBrandSizes()) {
                for (Well well : wells) {
                    indexMap.put(brandSize.getBrandSizeId() + "_" + well.getWellId(), idx++);
                }
            }
        }

        Long sessionId = distributionService.getSessionIdByDistribution(distributionId);
        List<StockroomInventory> stocks = stockroomService.getStockroomBySession(sessionId);

        model.addAttribute("stocks", stocks);
        model.addAttribute("brands", brands);
        model.addAttribute("wells", wells);
        model.addAttribute("stockMap", stockMap);
        model.addAttribute("indexMap", indexMap);
        model.addAttribute("distributionId", distributionId);
        model.addAttribute("wrapper", wrapper);
    }
}
