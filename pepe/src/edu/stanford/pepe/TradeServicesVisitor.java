package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.commons.AdviceAdapter;

/**
 * Instruments a JDBC connection so that on every commit/rollback it notifies
 * TransactionId.
 * 
 * @author jtamayo
 */
public class TradeServicesVisitor extends AdviceAdapter {

	private Method[] acceptedMethods = new Method[] {
			new Method("getMarketSummary", "()Lorg/apache/geronimo/samples/daytrader/MarketSummaryDataBean;"),
			new Method("buy",
					"(Ljava/lang/String;Ljava/lang/String;DI)Lorg/apache/geronimo/samples/daytrader/OrderDataBean;"),
			new Method("sell",
					"(Ljava/lang/String;Ljava/lang/Integer;I)Lorg/apache/geronimo/samples/daytrader/OrderDataBean;"),
			new Method("queueOrder", "(Ljava/lang/Integer;Z)V"),
			new Method("completeOrder", "(Ljava/lang/Integer;Z)Lorg/apache/geronimo/samples/daytrader/OrderDataBean;"),
			new Method("cancelOrder", "(Ljava/lang/Integer;Z)V"),
			new Method("orderCompleted", "(Ljava/lang/String;Ljava/lang/Integer;)V"),
			new Method("getOrders", "(Ljava/lang/String;)Ljava/util/Collection;"),
			new Method("getClosedOrders", "(Ljava/lang/String;)Ljava/util/Collection;"),
			new Method("createQuote",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/math/BigDecimal;)Lorg/apache/geronimo/samples/daytrader/QuoteDataBean;"),
			new Method("getQuote", "(Ljava/lang/String;)Lorg/apache/geronimo/samples/daytrader/QuoteDataBean;"),
			new Method("getAllQuotes", "()Ljava/util/Collection;"),
			new Method("updateQuotePriceVolume",
					"(Ljava/lang/String;Ljava/math/BigDecimal;D)Lorg/apache/geronimo/samples/daytrader/QuoteDataBean;"),
			new Method("getHoldings", "(Ljava/lang/String;)Ljava/util/Collection;"),
			new Method("getHolding", "(Ljava/lang/Integer;)Lorg/apache/geronimo/samples/daytrader/HoldingDataBean;"),
			new Method("getAccountData", "(Ljava/lang/String;)Lorg/apache/geronimo/samples/daytrader/AccountDataBean;"),
			new Method("getAccountProfileData",
					"(Ljava/lang/String;)Lorg/apache/geronimo/samples/daytrader/AccountProfileDataBean;"),
			new Method(
					"updateAccountProfile",
					"(Lorg/apache/geronimo/samples/daytrader/AccountProfileDataBean;)Lorg/apache/geronimo/samples/daytrader/AccountProfileDataBean;"),
			new Method("login",
					"(Ljava/lang/String;Ljava/lang/String;)Lorg/apache/geronimo/samples/daytrader/AccountDataBean;"),
			new Method("logout", "(Ljava/lang/String;)V"),
			new Method(
					"register",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/math/BigDecimal;)Lorg/apache/geronimo/samples/daytrader/AccountDataBean;"),
			new Method("resetTrade", "(Z)Lorg/apache/geronimo/samples/daytrader/RunStatsDataBean;"),
			new Method("runDaCapoTrade", "(Ljava/lang/String;IZ)V"),
			new Method("initializeDaCapo", "(Ljava/lang/String;)V"),
			new Method("resetDaCapo", "(Ljava/lang/String;I)Z"), };
	private String name;

	public TradeServicesVisitor(MethodVisitor mv, int access, String name, String desc) {
		super(mv, access, name, desc);
		this.name = name;
	}

	@Override
	protected void onMethodEnter() {
		for (Method method : acceptedMethods) {
			if (method.matches(this.name, this.methodDesc)) {
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitMethodInsn(INVOKESTATIC, "edu/stanford/pepe/runtime/TransactionId", "onNewTransaction", "()V");
			}
		}
	}

	private static class Method {
		String name;
		String signature;

		public Method(String name, String signature) {
			this.name = name;
			this.signature = signature;
		}

		public boolean matches(String name, String signature) {
			return this.name.equals(name) && this.signature.equals(signature);
		}

	}

}
