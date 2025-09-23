package org.example.be17pickcook.domain.order.model

import io.portone.sdk.server.payment.CancelPaymentResponse
import io.portone.sdk.server.payment.Payment
import io.portone.sdk.server.payment.PaymentClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

fun PaymentClient.getPaymentFuture(paymentId: String): CompletableFuture<Payment> {
    return GlobalScope.future { getPayment(paymentId) }
}

fun PaymentClient.cancelPaymentAsync(
    paymentId: String,
    amount: Long?,
    reason: String,
    currentCancellableAmount: Long?
): CompletableFuture<CancelPaymentResponse> {
    return GlobalScope.future {
        cancelPayment(
            paymentId = paymentId,
            amount = amount,
            reason = reason,
            currentCancellableAmount = currentCancellableAmount
        )
    }
}

fun PaymentClient.getPaymentAsync(paymentId: String): java.util.concurrent.CompletableFuture<Payment> =
    CoroutineScope(Dispatchers.IO).future { getPayment(paymentId) }