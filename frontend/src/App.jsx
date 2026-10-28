import { useState } from 'react'

function App() {
  const [cantidad, setCantidad] = useState(1)
  const [tarjeta, setTarjeta] = useState('')
  const [mensaje, setMensaje] = useState(null) // Objeto para manejar texto y tipo (éxito/error)
  const [procesando, setProcesando] = useState(false)

  const precio = 50.00
  const total = precio * cantidad

  const formatearTarjeta = (valor) => {
    const numeros = valor.replace(/\D/g, '').slice(0, 16)
    return numeros.replace(/(.{4})/g, '$1 ').trim()
  }

  const generarStan = () => {
    return String(Math.floor(100000 + Math.random() * 900000))
  }

  const realizarPago = async (event) => {
    event.preventDefault()

    const pan = tarjeta.replace(/\s/g, '')

    if (pan.length !== 16) {
      setMensaje({
        tipo: 'error',
        texto: 'Ingresa un número de tarjeta válido de 16 dígitos.'
      })
      return
    }

    setProcesando(true)
    setMensaje({ tipo: 'info', texto: 'Procesando pago con la pasarela ISO 8583...' })

    try {
      // Petición al endpoint del Ruteador/Pasarela en Spring Boot
      const respuesta = await fetch('http://localhost:8080/api/ruteador/pago', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          pan: pan,
          monto: total.toFixed(2),
          stan: generarStan(),
        }),
      })

      if (!respuesta.ok) {
        throw new Error(`Error HTTP: ${respuesta.status}`)
      }

      const datos = await respuesta.json()

      // Mapeo de Códigos de Respuesta ISO 8583 (DE 39)
      const codigo = String(datos.codigoRespuesta)

      if (codigo === '0' || codigo === '00') {
        setMensaje({
          tipo: 'exito',
          texto: `¡Pago Aprobado por ${datos.banco}! Transacción procesada correctamente.`
        })
      } else if (codigo === '51') {
        setMensaje({
          tipo: 'error',
          texto: 'Pago Rechazado: Fondos insuficientes en la cuenta.'
        })
      } else if (codigo === '57') {
        setMensaje({
          tipo: 'error',
          texto: 'Pago Rechazado: La cuenta o tarjeta no está activa.'
        })
      } else if (codigo === '14') {
        setMensaje({
          tipo: 'error',
          texto: 'Pago Rechazado: Tarjeta o cuenta inexistente.'
        })
      } else if (codigo === '13') {
        setMensaje({
          tipo: 'error',
          texto: 'Pago Rechazado: Monto inválido.'
        })
      } else {
        setMensaje({
          tipo: 'error',
          texto: `Pago Rechazado. Código de respuesta ISO: ${codigo}`
        })
      }

    } catch (error) {
      console.error('Error al conectar con la pasarela:', error)
      setMensaje({
        tipo: 'error',
        texto: 'No fue posible conectar con el servidor backend de la pasarela.'
      })
    } finally {
      setProcesando(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-100">

      <header className="bg-slate-950 text-white">
        <div className="max-w-6xl mx-auto px-6 py-5">
          <h1 className="text-2xl font-bold">
            Pasarela de Pagos ISO 8583
          </h1>
          <p className="text-slate-400 text-sm">
            Simulador de Adquirencia y Ruteo Financiero Cloud-Native
          </p>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-10">

        <div className="grid lg:grid-cols-2 gap-8">

          {/* Sección Producto */}
          <section className="bg-white rounded-2xl shadow-sm p-8">
            <div className="h-64 rounded-xl bg-slate-200 flex items-center justify-center mb-6">
              <span className="text-slate-500 text-lg font-medium">
                Imagen del Producto
              </span>
            </div>

            <h2 className="text-2xl font-bold text-slate-900">
              Producto Demostración
            </h2>

            <p className="text-slate-600 mt-2">
              Prueba de transacción financiera en tiempo real contra los Cores Bancarios A y B.
            </p>

            <div className="flex items-center justify-between mt-6">
              <span className="text-2xl font-bold text-slate-900">
                Q{precio.toFixed(2)}
              </span>

              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={() => setCantidad(Math.max(1, cantidad - 1))}
                  className="w-10 h-10 rounded-lg bg-slate-200 hover:bg-slate-300 font-bold text-slate-700 transition"
                >
                  −
                </button>

                <span className="font-semibold w-6 text-center text-slate-900">
                  {cantidad}
                </span>

                <button
                  type="button"
                  onClick={() => setCantidad(cantidad + 1)}
                  className="w-10 h-10 rounded-lg bg-slate-200 hover:bg-slate-300 font-bold text-slate-700 transition"
                >
                  +
                </button>
              </div>
            </div>
          </section>

          {/* Sección Formulario de Pago */}
          <section className="bg-white rounded-2xl shadow-sm p-8">

            <h2 className="text-2xl font-bold text-slate-900">
              Finalizar compra
            </h2>

            <p className="text-slate-500 mt-2 mb-8">
              Ingresa los 16 dígitos de la tarjeta de prueba.
            </p>

            <div className="bg-slate-50 rounded-xl p-5 mb-6">
              <div className="flex justify-between text-slate-600">
                <span>Precio unitario</span>
                <span>Q{precio.toFixed(2)}</span>
              </div>

              <div className="flex justify-between text-slate-600 mt-2">
                <span>Cantidad</span>
                <span>{cantidad}</span>
              </div>

              <div className="border-t border-slate-200 mt-4 pt-4 flex justify-between">
                <span className="font-bold text-slate-900">Total a debitar</span>
                <span className="text-xl font-bold text-slate-900">
                  Q{total.toFixed(2)}
                </span>
              </div>
            </div>

            <form onSubmit={realizarPago}>
              <label className="block text-sm font-semibold text-slate-700 mb-2">
                Número de tarjeta (PAN)
              </label>

              <input
                type="text"
                inputMode="numeric"
                placeholder="4111 1111 1111 1111"
                value={tarjeta}
                onChange={(e) => setTarjeta(formatearTarjeta(e.target.value))}
                className="w-full border border-slate-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500 font-mono text-slate-800"
              />

              <button
                type="submit"
                disabled={procesando}
                className="w-full mt-6 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-bold py-3 rounded-xl transition cursor-pointer disabled:cursor-not-allowed"
              >
                {procesando ? 'Procesando Transacción...' : `Pagar Q${total.toFixed(2)}`}
              </button>
            </form>

            {/* Alerta de Mensaje Dinámica */}
            {mensaje && (
              <div
                className={`mt-6 rounded-xl p-4 text-sm font-medium border ${
                  mensaje.tipo === 'exito'
                    ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
                    : mensaje.tipo === 'info'
                    ? 'bg-blue-50 border-blue-200 text-blue-800'
                    : 'bg-rose-50 border-rose-200 text-rose-800'
                }`}
              >
                {mensaje.texto}
              </div>
            )}

            <p className="text-xs text-slate-400 text-center mt-6">
              Conectado a Pasarela Spring Boot 4.1 vía REST / Sockets TCP
            </p>

          </section>

        </div>

      </main>
    </div>
  )
}

export default App