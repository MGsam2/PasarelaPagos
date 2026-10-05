import { useState } from 'react'

function App() {
  const [cantidad, setCantidad] = useState(1)
  const [tarjeta, setTarjeta] = useState('')
  const [mensaje, setMensaje] = useState('')
  const [procesando, setProcesando] = useState(false)

  const precio = 49.99
  const total = precio * cantidad

  const formatearTarjeta = (valor) => {
    const numeros = valor.replace(/\D/g, '').slice(0, 16)

    return numeros
      .replace(/(.{4})/g, '$1 ')
      .trim()
  }

  const generarStan = () => {
    return String(
      Math.floor(100000 + Math.random() * 900000)
    )
  }

  const realizarPago = async (event) => {
    event.preventDefault()

    const pan = tarjeta.replace(/\s/g, '')

    if (pan.length !== 16) {
      setMensaje(
        'Ingresa un número de tarjeta válido de 16 dígitos.'
      )
      return
    }

    setProcesando(true)
    setMensaje('Procesando pago...')

    try {
      const respuesta = await fetch(
        'http://localhost:8080/api/pagos/tarjeta',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            pan: pan,
            monto: total,
            stan: generarStan(),
          }),
        }
      )

      const datos = await respuesta.json()

      if (datos.codigoRespuesta === '00') {
        setMensaje(
          `Pago aprobado. Transacción #${datos.transaccionId}.`
        )
      } else if (datos.codigoRespuesta === '51') {
        setMensaje(
          'Pago rechazado: fondos insuficientes.'
        )
      } else if (datos.codigoRespuesta === '57') {
        setMensaje(
          'Pago rechazado: la cuenta no está activa.'
        )
      } else if (datos.codigoRespuesta === '13') {
        setMensaje(
          'Pago rechazado: monto inválido.'
        )
      } else {
        setMensaje(
          `Pago rechazado. Código: ${datos.codigoRespuesta}`
        )
      }

    } catch (error) {
      console.error(error)

      setMensaje(
        'No fue posible conectar con la pasarela de pagos.'
      )
    } finally {
      setProcesando(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-100">

      <header className="bg-slate-950 text-white">
        <div className="max-w-6xl mx-auto px-6 py-5">
          <h1 className="text-2xl font-bold">
            Pasarela de Pagos
          </h1>

          <p className="text-slate-400 text-sm">
            Compra segura con tarjeta de crédito
          </p>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-10">

        <div className="grid lg:grid-cols-2 gap-8">

          <section className="bg-white rounded-2xl shadow-sm p-8">

            <div className="h-64 rounded-xl bg-slate-200 flex items-center justify-center mb-6">
              <span className="text-slate-500 text-lg">
                Producto
              </span>
            </div>

            <h2 className="text-2xl font-bold text-slate-900">
              Producto Premium
            </h2>

            <p className="text-slate-600 mt-2">
              Producto de demostración para probar la
              pasarela de pagos.
            </p>

            <div className="flex items-center justify-between mt-6">

              <span className="text-2xl font-bold text-slate-900">
                Q{precio.toFixed(2)}
              </span>

              <div className="flex items-center gap-3">

                <button
                  onClick={() =>
                    setCantidad(
                      Math.max(1, cantidad - 1)
                    )
                  }
                  className="w-10 h-10 rounded-lg bg-slate-200 hover:bg-slate-300 font-bold"
                >
                  −
                </button>

                <span className="font-semibold w-6 text-center">
                  {cantidad}
                </span>

                <button
                  onClick={() =>
                    setCantidad(cantidad + 1)
                  }
                  className="w-10 h-10 rounded-lg bg-slate-200 hover:bg-slate-300 font-bold"
                >
                  +
                </button>

              </div>
            </div>
          </section>

          <section className="bg-white rounded-2xl shadow-sm p-8">

            <h2 className="text-2xl font-bold text-slate-900">
              Finalizar compra
            </h2>

            <p className="text-slate-500 mt-2 mb-8">
              Ingresa los datos de tu tarjeta.
            </p>

            <div className="bg-slate-50 rounded-xl p-5 mb-6">

              <div className="flex justify-between text-slate-600">
                <span>Producto</span>
                <span>Q{precio.toFixed(2)}</span>
              </div>

              <div className="flex justify-between text-slate-600 mt-2">
                <span>Cantidad</span>
                <span>{cantidad}</span>
              </div>

              <div className="border-t border-slate-200 mt-4 pt-4 flex justify-between">
                <span className="font-bold text-slate-900">
                  Total
                </span>

                <span className="text-xl font-bold text-slate-900">
                  Q{total.toFixed(2)}
                </span>
              </div>

            </div>

            <form onSubmit={realizarPago}>

              <label className="block text-sm font-semibold text-slate-700 mb-2">
                Número de tarjeta
              </label>

              <input
                type="text"
                inputMode="numeric"
                placeholder="4111 1111 1111 1111"
                value={tarjeta}
                onChange={(event) =>
                  setTarjeta(
                    formatearTarjeta(
                      event.target.value
                    )
                  )
                }
                className="w-full border border-slate-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
              />

              <button
                type="submit"
                disabled={procesando}
                className="w-full mt-6 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-bold py-3 rounded-xl transition"
              >
                {procesando
                  ? 'Procesando...'
                  : `Pagar Q${total.toFixed(2)}`}
              </button>

            </form>

            {mensaje && (
              <div className="mt-6 bg-blue-50 border border-blue-200 text-blue-700 rounded-xl p-4">
                {mensaje}
              </div>
            )}

            <p className="text-xs text-slate-400 text-center mt-6">
              Pago procesado mediante la Pasarela de Pagos
            </p>

          </section>

        </div>

      </main>
    </div>
  )
}

export default App