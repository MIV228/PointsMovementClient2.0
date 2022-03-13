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
    // создание и настройка окна
    val window = MainFrame()
    window.setSize(800,800)
    window.isVisible = true
}
class PlayFrame {
    // подключение к бд
    // запуск чтения с бд
    // цикл отображение кругов по координатам? прочитаных из бд
}
class MainFrame : JFrame(){
    // настройка websocket клиента
    val client = HttpClient(CIO){
        install(WebSockets)
    }
    // состояние позиции игрока х и у
    val playerPosition : MutableStateFlow<Pair<Int,Int>> = MutableStateFlow(0 to 0)
    init {
        GlobalScope.launch(Dispatchers.IO) {
            // подключение к сокету
            client.webSocket(host = "0.0.0.0", port = 8080) {
                launch {
                    // при обновлении положения мыши отправлять новое положение на сервер
                    playerPosition.collect {
                        outgoing.send(Frame.Text("${it.first},${it.second}"))
                    }
                }
                // когда с сервера приходят новые данные
                for (data in incoming){
                    if (data is Frame.Text){
                        val positions = data.readText()
                        // очищаем список
                        players.clear()
                        // расшифровываем позиции игроков из строки и помещаем в общий список
                        // 868,31;779,32;695,35 <- формат приходящих данных, х1,у1;х2,у2.....
                        players.addAll(pairs(positions))
                        println(positions)
                        // вызов перерисовки
                        repaint()
                    }
                }
            }
        }
        // добавление слушателя для перемещения мыши по экрану
        addMouseMotionListener(object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent?) {
            }

            override fun mouseMoved(e: MouseEvent) {
                // обновление значения позиции мыши
                playerPosition.value = e.x to e.y
            }
        })
    }

    private fun pairs(positions: String): List<Pair<Int,Int>> = positions.split(";").map { position ->
        val pos = position.split(",")
        pos.first().toInt() to pos.last().toInt()
    }

    // положение всех точек на плоскости
    val players = Collections.synchronizedList(mutableListOf<Pair<Int,Int>>())
    override fun paint(g: Graphics) {
        // очистка ранее нарисованных фигур
        g.clearRect(0,0,width, height)
        // рисование круга для каждого игрока
        players.forEach { player ->
            g.fillOval(player.first, player.second, 100, 100)
        }
    }
}