package com.skillw.fightsystem.internal.feature.compat.mythicmobs.common

import com.skillw.fightsystem.api.fight.DataCache
import com.skillw.pouvoir.util.decodeFromString
import com.skillw.pouvoir.util.encodeJson
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.core.logging.MythicLogger
import io.lumine.mythic.core.skills.variables.Variable
import io.lumine.mythic.core.skills.variables.VariableType
import org.bukkit.entity.LivingEntity

/**
 * @className AttributeCacheV
 *
 * @author Glom
 * @date 2023年1月21日 8:14 Copyright 2022 user. All rights reserved.
 */
internal class AttributeCacheV(private val config: MythicLineConfig) :
    DamageMechanic(config) {
    val key: PlaceholderString =
        PlaceholderString.of(config.getString(arrayOf("key", "k"), "null"))
    val type: PlaceholderString =
        PlaceholderString.of(config.getString(arrayOf("type", "t"), "attacker"))
    val expire: PlaceholderString =
        PlaceholderString.of(config.getString(arrayOf("expire", "e"), "0"))

    override fun castAtEntity(data: SkillMetadata, targetAE: AbstractEntity): SkillResult {
        val target = targetAE.bukkitEntity
        val key = key.get(data, targetAE)
        val type = type.get(data, targetAE)
        val expire = expire.get(data, targetAE).toLong()
        if (target is LivingEntity && !target.isDead) {
            when (type) {
                "attacker", "a", "attack" -> {
                    if (data.variables.has(key)) {
                        val cache = data.variables.getString(key).decodeFromString<DataCache>()
                        cache ?: data.variables.remove(key)
                        cache?.attacker(target)
                    }
                    if (!data.variables.has(key))
                        data.variables.put(
                            key,
                            Variable.ofType(VariableType.STRING, DataCache().attacker(target).encodeJson(), expire)
                        )
                }

                "defender", "d", "defend" -> {
                    if (data.variables.has(key)) {
                        val cache = data.variables.getString(key).decodeFromString<DataCache>()
                        cache ?: data.variables.remove(key)
                        cache?.defender(target)
                    }
                    if (!data.variables.has(key))
                        data.variables.put(
                            key,
                            Variable.ofType(VariableType.STRING, DataCache().defender(target).encodeJson(), expire)
                        )
                }
            }

            MythicLogger.debug(
                MythicLogger.DebugLevel.MECHANIC,
                "+ AttributeCache fired for {0} with key {1}",
                target, key
            )
        }
        return SkillResult.SUCCESS
    }


}
