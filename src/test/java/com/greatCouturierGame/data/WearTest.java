package com.greatCouturierGame.data;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WearTest {

    @Test
    void generateRandomWear() throws Exception {
        List<Integer> maxClothesIds = Arrays.asList(
                867
        );
        Map<Wear.Parameters, Integer> clothesParametersIds = new HashMap<>();
        clothesParametersIds.put(Wear.Parameters.COLOR, 156);
        clothesParametersIds.put(Wear.Parameters.TEXTURE, 256);
        clothesParametersIds.put(Wear.Parameters.TEXTURE_COLOR, 337);
        Wear randomClothes = Wear.generateRandomWear(maxClothesIds, clothesParametersIds);

        assertTrue(randomClothes.getWearType() > 801 && randomClothes.getWearType() <= 867);
        assertTrue(randomClothes.getWearColor() > 101 && randomClothes.getWearColor() <= 156);
        assertTrue(randomClothes.getWearTexture() > 201 && randomClothes.getWearTexture() <= 256);
        assertTrue(randomClothes.getWearTextureColor() > 301 && randomClothes.getWearTextureColor() <= 337);
    }
}