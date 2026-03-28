package com.shambasmart.maarifa.rules

import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.data.local.entity.maarifa.OperationalRule
import org.json.JSONObject
import java.time.LocalDate

class RuleEngine(private val ruleDao: OperationalRuleDao) {

    suspend fun calculateWithdrawal(drugGeneric: String, species: String, treatmentDate: LocalDate): WithdrawalResult? {
        val rule = ruleDao.getWithdrawalRule(drugGeneric, species) ?: return null
        val params = try { JSONObject(rule.parametersJson) } catch (e: Exception) { return null }
        val milkDays = params.optInt("milk_withdrawal_days", -1)
        val meatDays = params.optInt("meat_withdrawal_days", -1)
        val buffer = params.optInt("conservative_buffer_days", 0)
        return WithdrawalResult(drugGeneric, treatmentDate,
            if (milkDays > 0) treatmentDate.plusDays((milkDays + buffer).toLong()) else null,
            if (meatDays > 0) treatmentDate.plusDays((meatDays + buffer).toLong()) else null,
            rule.source, rule.lastVerified)
    }

    suspend fun calculateDose(drugGeneric: String, species: String, bodyWeightKg: Double): DoseResult? {
        val rule = ruleDao.getDoseRule(drugGeneric, species) ?: return null
        val params = try { JSONObject(rule.parametersJson) } catch (e: Exception) { return null }
        val mgPerKg = params.optDouble("mg_per_kg", 0.0)
        val mlPerKg = params.optDouble("ml_per_kg", 0.0)
        val maxMl = params.optDouble("max_dose_ml", Double.MAX_VALUE)
        val route = params.optString("route", "SC")
        val actualMl = minOf(mlPerKg * bodyWeightKg, maxMl)
        return DoseResult(drugGeneric, bodyWeightKg, mgPerKg * bodyWeightKg, actualMl, route, rule.source)
    }

    suspend fun calculateDueDate(species: String, matingDate: LocalDate): GestationResult? {
        val rule = ruleDao.getGestationRule(species) ?: return null
        val params = try { JSONObject(rule.parametersJson) } catch (e: Exception) { return null }
        val days = params.optInt("gestation_days", 150)
        val preDays = params.optInt("pre_kidding_task_days", 7)
        val due = matingDate.plusDays(days.toLong())
        return GestationResult(species, matingDate, due, due.minusDays(preDays.toLong()), days, rule.source)
    }

    suspend fun checkNotifiable(diseaseName: String): NotifiableResult? {
        val rule = ruleDao.getNotifiableRule(diseaseName) ?: return null
        val params = try { JSONObject(rule.parametersJson) } catch (e: Exception) { return null }
        return NotifiableResult(diseaseName, params.optString("reporting_body", "TVLA"),
            params.optString("reporting_body_full", "Tanzania Veterinary Laboratory Agency"),
            params.optString("action", "Contact TVLA immediately."), rule.source)
    }

    suspend fun getPlantingWindow(crop: String, location: String): PlantingWindowResult? {
        val rules = ruleDao.getPlantingRules("planting_window", crop, location)
        if (rules.isEmpty()) return null
        val rule = rules.first()
        val params = try { JSONObject(rule.parametersJson) } catch (e: Exception) { return null }
        return PlantingWindowResult(crop, location, params.optString("season", "long_rains"),
            params.optInt("optimal_start_month", 3), params.optInt("optimal_end_month", 4), rule.source)
    }

    data class WithdrawalResult(val drugGeneric: String, val treatmentDate: LocalDate,
        val milkSafeDate: LocalDate?, val meatSafeDate: LocalDate?, val source: String, val lastVerified: String)
    data class DoseResult(val drugGeneric: String, val bodyWeightKg: Double,
        val totalMg: Double, val recommendedMl: Double, val route: String, val source: String)
    data class GestationResult(val species: String, val matingDate: LocalDate,
        val expectedDueDate: LocalDate, val preEventTaskDate: LocalDate, val gestationDays: Int, val source: String)
    data class NotifiableResult(val diseaseName: String, val reportingBody: String,
        val reportingBodyFull: String, val action: String, val source: String)
    data class PlantingWindowResult(val crop: String, val location: String,
        val season: String, val optimalStartMonth: Int, val optimalEndMonth: Int, val source: String)
}