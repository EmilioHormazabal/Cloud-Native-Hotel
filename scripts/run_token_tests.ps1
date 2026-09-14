# Uso: 1) pausa, pega el token en el prompt. 2) $env:TOKEN="eyJ..." ; pwsh .\run_token_tests.ps1
$ErrorActionPreference = "Stop"
$token = $env:TOKEN
if (-not $token) {
    $token = Read-Host "Pega el token (eyJ...) y presiona Enter"
}
$H = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
$inv = { param($m,$u,$b=$null) try {
    $r = if($m -eq 'GET'){ Invoke-RestMethod -Method GET -Uri $u -Headers $H -TimeoutSec 10 }
         else { Invoke-RestMethod -Method $m -Uri $u -Headers $H -Body ($b|ConvertTo-Json) -TimeoutSec 10 }
    [pscustomobject]@{ ok = $true; data = $r; error = $null }
} catch { [pscustomobject]@{ ok = $false; data = $null; error = "$($_.Exception.Response.StatusCode.value__) $($_.Exception.Message)" } } }

echo "===== 1.1 VALIDACION JWT REAL -> 200 (3 micros) ====="
foreach ($u in @("http://localhost:8082/api/v1/servicio/list","http://localhost:8081/api/v1/reserva/list","http://localhost:8083/api/v1/usuario/list")) {
    $r = & $inv "GET" $u
    if ($r.ok) { "$u -> OK ($($r.data.Count) registros)" } else { "$u -> ERROR $($r.error)" }
}

echo "===== 1.3 CRUD ESCRITURA (Panel admin simulado) ====="
$nuevo = @{ nombre="Servicio QA $([DateTime]::Now.ToString('HHmmss'))"; tipoServicio="habitacion"; numHabitacion=777; nivelServicio="invitado"; descripcion="creado por prueba QA"; precio=42000; capacidad=2; disponible=$true }
$creado = & $inv "POST" "http://localhost:8082/api/v1/servicio/post" $nuevo
$id = $creado.data.id
if ($creado.ok -and $id) {
    "POST /post -> creado id=$id"
    $editado = & $inv "PUT" "http://localhost:8082/api/v1/servicio/put/$id" (@{ nombre="Servicio QA editado"; tipoServicio="comida"; numHabitacion=1; nivelServicio="miembro"; descripcion="editado por QA"; precio=999; capacidad=1; disponible=$false })
    "PUT /put/$id -> $(if($editado.ok){$editado.data.nombre}else{'ERROR '+$editado.error})"
    $verif = & $inv "GET" "http://localhost:8082/api/v1/servicio/get/$id"
    if ($verif.ok -and $verif.data.nombre -eq "Servicio QA editado") { "BD refleja edicion: OK ($($verif.data.nombre) / $($verif.data.precio))" } else { "BD refleja edicion: FALLO -> $($verif.error)" }
    & $inv "DELETE" "http://localhost:8082/api/v1/servicio/delete/$id" | Out-Null
    "DELETE /delete/$id -> limpieza OK"
} else {
    "POST /post -> FALLO: $(if(-not $creado.ok){$creado.error}else{"sin id en respuesta"})"
}

echo "===== 1.2 CARGA CON JWT REAL (N=200 C=20 a /servicio/list) ====="
$sw = [Diagnostics.Stopwatch]::StartNew()
$res = @(1..200 | ForEach-Object -Parallel {
    $h = [Net.Http.HttpClient]::new(); $h.Timeout = [TimeSpan]::FromSeconds(20)
    $out = [pscustomobject]@{ ok = $false; ms = 0.0 }
    $t0 = $null
    try {
        $req = [Net.Http.HttpRequestMessage]::new('GET', 'http://localhost:8082/api/v1/servicio/list')
        $req.Headers.Authorization = [Net.Http.Headers.AuthenticationHeaderValue]::new('Bearer', $using:token)
        $t0 = [Diagnostics.Stopwatch]::StartNew()
        $r = $h.SendAsync($req).GetAwaiter().GetResult()
        $out.ms = [double]$t0.Elapsed.TotalMilliseconds
        if ($r.StatusCode -eq 200) { $out.ok = $true }
    } catch { if ($t0) { $out.ms = [double]$t0.Elapsed.TotalMilliseconds } } finally { $h.Dispose() }
    $out
} -ThrottleLimit 20)
$sw.Stop()
$ok = ($res | Where-Object ok).Count
$err = $res.Count - $ok
$lats = @($res | ForEach-Object ms | Where-Object { $_ -gt 0 }) | Sort-Object
if ($lats.Count -gt 0) {
    $p50 = $lats[[int]($lats.Count * 0.5)].ToString('0.0')
    $p95 = $lats[[int]($lats.Count * 0.95)].ToString('0.0')
} else { $p50 = 'n/a'; $p95 = 'n/a' }
$sumMs = ($res | Measure-Object ms -Sum).Sum / 1000
$rps = if ($sumMs -gt 0) { [math]::Round($res.Count / $sumMs, 1) } else { 0 }
"CARGA AUTENTICADA (200 req) -> 200=$ok errores=$err | t=$($sw.Elapsed.TotalSeconds.ToString('0.0'))s | rps=$rps | p50=$p50 p95=$p95"