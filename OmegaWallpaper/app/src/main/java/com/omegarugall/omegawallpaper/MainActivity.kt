package com.omegarugall.omegawallpaper

import android.app.WallpaperManager
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OmegaApp() }
    }
}

data class Wallpaper(
    val id: String,
    val title: String,
    val category: String,
    val remoteUrl: String? = null,
    val localRes: Int? = null
) {
    val isOffline: Boolean get() = localRes != null
}

private val offlineWallpapers = listOf(
    Wallpaper("o1","Nebula Blue","Abstract",localRes=R.drawable.offline_1),
    Wallpaper("o2","Solar Bloom","Abstract",localRes=R.drawable.offline_2),
    Wallpaper("o3","Forest Dream","Nature",localRes=R.drawable.offline_3),
    Wallpaper("o4","Purple Orbit","Abstract",localRes=R.drawable.offline_4),
    Wallpaper("o5","Golden Dunes","Nature",localRes=R.drawable.offline_5),
    Wallpaper("o6","Deep Ocean","Nature",localRes=R.drawable.offline_6)
)

private val onlineWallpapers = listOf(
    Wallpaper("n1","Mountain Light","Nature","https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=900&q=85"),
    Wallpaper("n2","Ocean Calm","Nature","https://images.unsplash.com/photo-1500375592092-40eb2168fd21?w=900&q=85"),
    Wallpaper("n3","Night Sky","Nature","https://images.unsplash.com/photo-1470770841072-f978cf4d019e?w=900&q=85"),
    Wallpaper("n4","Minimal Architecture","Architecture","https://images.unsplash.com/photo-1511818966892-d7d671e672a2?w=900&q=85"),
    Wallpaper("n5","Desert Lines","Nature","https://images.unsplash.com/photo-1509316785289-025f5b846b35?w=900&q=85"),
    Wallpaper("n6","Neon City","City","https://images.unsplash.com/photo-1519608487953-e999c86e7455?w=900&q=85"),
    Wallpaper("n7","Green Escape","Nature","https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=900&q=85"),
    Wallpaper("n8","Blue Abstract","Abstract","https://images.unsplash.com/photo-1557683316-973673baf926?w=900&q=85")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmegaApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("omega_wallpaper", 0) }
    val favorites = remember { mutableStateListOf<String>().apply {
        addAll(prefs.getStringSet("favorites", emptySet()) ?: emptySet())
    } }
    var tab by remember { mutableStateOf("Explorar") }
    var category by remember { mutableStateOf("Todos") }
    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Wallpaper?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val all = offlineWallpapers + onlineWallpapers
    val source = when(tab) {
        "Favoritos" -> all.filter { favorites.contains(it.id) }
        "Offline" -> offlineWallpapers
        else -> all
    }
    val shown = source.filter {
        (category == "Todos" || it.category == category) &&
        (it.title.contains(search, true) || it.category.contains(search, true))
    }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); message = null }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFF9C8CFF),
        secondary = Color(0xFF61D6D0),
        background = Color(0xFF090B12),
        surface = Color(0xFF151925)
    )) {
        Scaffold(
            containerColor = Color(0xFF090B12),
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("OMEGA", fontWeight = FontWeight.Black, letterSpacing = 3.sp, color = Color.White)
                            Text("WALLPAPER", fontSize = 10.sp, letterSpacing = 4.sp, color = Color(0xFF9C8CFF))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B12)),
                    actions = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF9C8CFF), modifier = Modifier.padding(end=18.dp))
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF121622)) {
                    listOf("Explorar" to Icons.Default.Explore, "Favoritos" to Icons.Default.Favorite, "Offline" to Icons.Default.Download).forEach { (name, icon) ->
                        NavigationBarItem(
                            selected = tab == name,
                            onClick = { tab = name; category = "Todos" },
                            icon = { Icon(icon, contentDescription = name) },
                            label = { Text(name) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFB6A9FF), indicatorColor = Color(0xFF292443), selectedTextColor = Color.White, unselectedTextColor = Color.Gray)
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Text(
                    when(tab) { "Offline" -> "Seus wallpapers sem internet"; "Favoritos" -> "Sua coleção pessoal"; else -> "Encontre seu próximo visual" },
                    color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start=18.dp, top=12.dp, bottom=12.dp)
                )
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal=16.dp),
                    placeholder = { Text("Buscar wallpapers...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9C8CFF),
                        unfocusedBorderColor = Color(0xFF34394A),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )
                Row(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todos","Nature","Abstract","City","Architecture").forEach { item ->
                        FilterChip(
                            selected = category == item, onClick = { category = item },
                            label = { Text(when(item) { "Abstract" -> "Abstrato"; "Architecture" -> "Arquitetura"; "Todos" -> "Todos"; else -> item }) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF342D59), selectedLabelColor = Color.White)
                        )
                    }
                }
                if (tab == "Explorar") {
                    Row(Modifier.padding(horizontal=18.dp).fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                        Text("Coleção em destaque", color=Color.White, fontWeight=FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("HD • 4K", color=Color(0xFF61D6D0), fontSize=12.sp)
                    }
                }
                if (shown.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center) {
                        Text(if (tab == "Favoritos") "Você ainda não favoritou nenhum wallpaper." else "Nenhum wallpaper encontrado.", color=Color.LightGray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(shown, key={it.id}) { wallpaper ->
                            WallpaperTile(
                                wallpaper=wallpaper,
                                favorite=favorites.contains(wallpaper.id),
                                onFavorite={
                                    if (favorites.contains(wallpaper.id)) favorites.remove(wallpaper.id) else favorites.add(wallpaper.id)
                                    prefs.edit().putStringSet("favorites", favorites.toSet()).apply()
                                },
                                onClick={ selected=wallpaper }
                            )
                        }
                    }
                }
            }
        }
    }

    selected?.let { wallpaper ->
        WallpaperDetails(
            wallpaper=wallpaper,
            onDismiss={ selected=null },
            onSet={
                scope.launch {
                    val bitmap = loadBitmap(context, wallpaper)
                    if (bitmap != null) {
                        try {
                            withContext(Dispatchers.IO) {
                                WallpaperManager.getInstance(context).setBitmap(bitmap)
                            }
                            message = "Papel de parede aplicado com sucesso!"
                        } catch (e: Exception) { message = "Não foi possível aplicar: ${e.localizedMessage}" }
                    } else message = "Não foi possível carregar a imagem."
                    selected=null
                }
            },
            onSave={
                scope.launch {
                    val bitmap = loadBitmap(context, wallpaper)
                    if (bitmap != null) {
                        val saved = saveToGallery(context, bitmap, wallpaper.title)
                        message = if(saved) "Imagem salva na galeria." else "Falha ao salvar imagem."
                    } else message = "Não foi possível carregar a imagem."
                }
            }
        )
    }
}

@Composable
fun WallpaperTile(wallpaper: Wallpaper, favorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(245.dp).clip(RoundedCornerShape(18.dp)).clickable(onClick=onClick)) {
        WallpaperImage(wallpaper, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.12f)))
        IconButton(onClick=onFavorite, modifier=Modifier.align(Alignment.TopEnd).padding(5.dp)) {
            Icon(if(favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint=if(favorite) Color(0xFFFF6B91) else Color.White)
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(alpha=.48f)).padding(11.dp)) {
            Text(wallpaper.title, color=Color.White, fontWeight=FontWeight.SemiBold, maxLines=1)
            Text(if(wallpaper.isOffline) "DISPONÍVEL OFFLINE" else wallpaper.category.uppercase(), color=Color(0xFFD0C8FF), fontSize=9.sp, letterSpacing=1.sp)
        }
    }
}

@Composable
fun WallpaperImage(wallpaper: Wallpaper, modifier: Modifier = Modifier) {
    if (wallpaper.localRes != null) {
        androidx.compose.foundation.Image(
            painter=androidx.compose.ui.res.painterResource(wallpaper.localRes),
            contentDescription=wallpaper.title, modifier=modifier, contentScale=ContentScale.Crop
        )
    } else {
        AsyncImage(
            model=wallpaper.remoteUrl, contentDescription=wallpaper.title,
            modifier=modifier, contentScale=ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetails(wallpaper: Wallpaper, onDismiss: () -> Unit, onSet: () -> Unit, onSave: () -> Unit) {
    ModalBottomSheet(onDismissRequest=onDismiss, containerColor=Color(0xFF151925)) {
        Column(Modifier.fillMaxWidth().padding(horizontal=20.dp).padding(bottom=28.dp)) {
            Box(Modifier.fillMaxWidth().height(430.dp).clip(RoundedCornerShape(20.dp))) {
                WallpaperImage(wallpaper, Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(16.dp))
            Text(wallpaper.title, color=Color.White, fontSize=22.sp, fontWeight=FontWeight.Bold)
            Text(if(wallpaper.isOffline) "Incluído no aplicativo • funciona sem internet" else "Wallpaper online • requer conexão para carregar", color=Color.LightGray, fontSize=12.sp)
            Spacer(Modifier.height(18.dp))
            Button(onClick=onSet, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Wallpaper, null); Spacer(Modifier.width(8.dp)); Text("Definir como papel de parede")
            }
            OutlinedButton(onClick=onSave, modifier=Modifier.fillMaxWidth(), shape=RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Download, null); Spacer(Modifier.width(8.dp)); Text("Salvar na galeria")
            }
        }
    }
}

private suspend fun loadBitmap(context: android.content.Context, wallpaper: Wallpaper): Bitmap? {
    return try {
        if (wallpaper.localRes != null) {
            (context.getDrawable(wallpaper.localRes) as BitmapDrawable).bitmap
        } else {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context).data(wallpaper.remoteUrl).allowHardware(false).build()
            val result = loader.execute(request)
            (result as? SuccessResult)?.drawable?.let { (it as? BitmapDrawable)?.bitmap }
        }
    } catch (_: Exception) { null }
}

private suspend fun saveToGallery(context: android.content.Context, bitmap: Bitmap, title: String): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Omega_${title.replace(" ","_")}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Omega Wallpaper")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext false
            context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) } ?: return@withContext false
            true
        } catch (_: Exception) { false }
    }
