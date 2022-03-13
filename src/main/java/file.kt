import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.util.*
import javax.swing.JFrame

fun main() {
    val window = MainFrame()
    window.setSize(800,800)
    window.isVisible = true
}

class MainFrame : JFrame(){

    val client = HttpClient(CIO){
        install(WebSockets)
    }
    val playerPosition : MutableStateFlow<Pair<Int,Int>> = MutableStateFlow(0 to 0)
    init {
        GlobalScope.launch(Dispatchers.IO) {
            client.webSocket(host = "0.0.0.0", port = 8080) {
                launch {
                    playerPosition.collect {
                        outgoing.send(Frame.Text("${it.first},${it.second}"))
                    }
                }
                for (data in incoming){
                    if (data is Frame.Text){
                        val positions = data.readText()
                        players.clear()
                        players.addAll(positions.split(";").map { position ->
                            val pos = position.split(",")
                            pos.first().toInt() to pos.last().toInt()
                        })
                        println(positions)
                        repaint()
                    }
                }
            }
        }
        addMouseMotionListener(object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent?) {
            }

            override fun mouseMoved(e: MouseEvent) {
                playerPosition.value = e.x to e.y
            }

        })
    }

    val players = Collections.synchronizedList(mutableListOf<Pair<Int,Int>>())
    override fun paint(g: Graphics) {
        g.clearRect(0,0,800,800)
        players.forEach { player ->
            g.fillOval(player.first, player.second, 100, 100)
        }
    }
}