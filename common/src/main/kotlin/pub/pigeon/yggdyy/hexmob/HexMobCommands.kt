package pub.pigeon.yggdyy.hexmob

import com.mojang.brigadier.arguments.FloatArgumentType
import dev.architectury.event.events.common.CommandRegistrationEvent
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig

/** 服务端调试/管理命令。 */
object HexMobCommands {
    fun init() {
        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("hexmob")
                    .requires { src -> src.hasPermission(2) }
                    .then(
                        Commands.literal("scale")
                            .then(
                                Commands.argument(
                                    "value",
                                    FloatArgumentType.floatArg(
                                        HexMobServerConfig.MIN_UR_CIRCLE_SCALE,
                                        HexMobServerConfig.MAX_UR_CIRCLE_SCALE,
                                    ),
                                ).executes { ctx ->
                                    val value = FloatArgumentType.getFloat(ctx, "value")
                                    HexMobServerConfig.setUrCircleScale(value)
                                    ctx.source.sendSuccess(
                                        { Component.literal("大环体积倍率已设为 $value（已同步所有在线玩家）") },
                                        true,
                                    )
                                    1
                                },
                            )
                            .executes { ctx ->
                                val cur = HexMobServerConfig.config.urCircleScale
                                ctx.source.sendSuccess(
                                    { Component.literal("当前大环体积倍率：$cur（范围 ${HexMobServerConfig.MIN_UR_CIRCLE_SCALE}~${HexMobServerConfig.MAX_UR_CIRCLE_SCALE}，用法 /hexmob scale <倍率>）") },
                                    true,
                                )
                                1
                            },
                    ),
            )
        }
    }
}
