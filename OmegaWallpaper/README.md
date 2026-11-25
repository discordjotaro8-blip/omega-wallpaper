# Omega Wallpaper

Aplicativo Android de wallpapers online e offline, feito com Kotlin + Jetpack Compose.

## Recursos
- Galeria com wallpapers online (imagens hospedadas no Unsplash) e 6 wallpapers originais incluídos no APK.
- Categorias e busca.
- Favoritos salvos localmente no aparelho.
- Definir imagem como papel de parede.
- Salvar imagem na galeria em `Pictures/Omega Wallpaper`.
- Aba Offline com imagens empacotadas no aplicativo, sem necessidade de internet.

## Requisitos
- Android Studio (versão recente)
- JDK 17
- Android SDK 35

## Abrir e executar
1. Extraia o ZIP.
2. No Android Studio, escolha **Open** e selecione a pasta `OmegaWallpaper`.
3. Aguarde o Gradle Sync.
4. Conecte seu Android com depuração USB ou use um emulador.
5. Clique em **Run**.

## Gerar APK
No Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

Ou no terminal, com Gradle instalado:
```bash
gradle assembleDebug
```
O APK de debug será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Publicar no GitHub
1. Crie um repositório chamado `omega-wallpaper`.
2. Extraia o projeto e abra o terminal na pasta.
3. Execute:
```bash
git init
git add .
git commit -m "Initial commit - Omega Wallpaper"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/omega-wallpaper.git
git push -u origin main
```

## Observações
- Os wallpapers online usam URLs públicas de imagens do Unsplash. É possível trocar ou ampliar a lista em `MainActivity.kt`.
- Os wallpapers offline ficam em `app/src/main/res/drawable-nodpi/`.
- O projeto não precisa de chave de API.
- Antes de publicar comercialmente, revise os termos/licenças e a disponibilidade das imagens remotas utilizadas.
