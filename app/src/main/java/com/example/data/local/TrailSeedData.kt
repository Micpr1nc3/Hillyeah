package com.example.data.local

import com.example.data.local.seeds.EasternCapeTrails
import com.example.data.local.seeds.FreeStateTrails
import com.example.data.local.seeds.GautengTrails
import com.example.data.local.seeds.KznTrails
import com.example.data.local.seeds.LimpopoTrails
import com.example.data.local.seeds.MpumalangaTrails
import com.example.data.local.seeds.NorthWestTrails
import com.example.data.local.seeds.NorthernCapeTrails
import com.example.data.local.seeds.WesternCapeTrails

object TrailSeedData {
    fun getAllTrails(): List<TrailEntity> =
        WesternCapeTrails.trails +
        GautengTrails.trails +
        KznTrails.trails +
        MpumalangaTrails.trails +
        EasternCapeTrails.trails +
        FreeStateTrails.trails +
        LimpopoTrails.trails +
        NorthWestTrails.trails +
        NorthernCapeTrails.trails
}
