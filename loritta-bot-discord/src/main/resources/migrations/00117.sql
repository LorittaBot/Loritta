INSERT INTO BomDiaECiaConfigs (id, enabled, blocked_channels, use_blocked_channels_as_allowed_channels)
SELECT sc.id, mc.enable_bom_dia_e_cia, ARRAY[]::bigint[], false
FROM ServerConfigs sc INNER JOIN MiscellaneousConfigs mc ON sc.miscellaneous_config = mc.id
WHERE mc.enable_bom_dia_e_cia = TRUE;