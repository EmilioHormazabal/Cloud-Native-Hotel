# Copyright (c) 2026 Emilio Hormazabal
# Uso: 1) abre https://localhost:5173 logueado como admin, F12 -> Consola.
#      2) pega el bloque "console snippet" (lineas marcadas abajo) y Enter.
#      3) el token queda en el portapapeles. Luego en PowerShell (repo raiz):
#         pwsh scripts\token_e2e.ps1   -> pega el token en el prompt
# Este wrapper: (a) pide el token, (b) corre run_token_tests.ps1,
#               (c) guarda el reporte en scripts\e2e_report.txt (no se commitea: ver .gitignore)
$token = $env:TOKEN
if (-not $token) { $token = Read-Host "Pega el token (eyJ...) y presiona Enter" }
$env:TOKEN = $token
$out = pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "run_token_tests.ps1") 2>&1
$out | Tee-Object -FilePath (Join-Path $PSScriptRoot "e2e_report.txt") | Write-Output

Write-Host ""
Write-Host "Cierre limpio. Reporte: scripts\e2e_report.txt" -ForegroundColor Green

# = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
# CONSOLE SNIPPET (pegar en F12 -> Consola estando logueado en localhost:5173):
# (function() {
#   var k = Object.keys(localStorage).filter(x => x.indexOf('msal') > -1);
#   var t = '';
#   for (var i=0;i<k.length;i++){
#     try {
#       var v = JSON.parse(localStorage.getItem(k[i]));
#       if (v && v.accessToken) { t = v.accessToken; break; }
#     } catch(e){}
#   }
#   if (!t) { console.error('No se encontro accessToken en localStorage. Re-loguea.'); return; }
#   var ta = document.createElement('textarea');
#   ta.value = t;
#   document.body.appendChild(ta);
#   ta.select();
#   document.execCommand('copy');
#   document.body.removeChild(ta);
#   console.log('Token copiado ('+t.length+' chars)');
# })();
# = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =