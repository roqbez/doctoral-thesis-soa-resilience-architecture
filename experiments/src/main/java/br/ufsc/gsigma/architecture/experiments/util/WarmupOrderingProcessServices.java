package br.ufsc.gsigma.architecture.experiments.util;

import br.ufsc.gsigma.services.resilience.support.ResilienceUtil;
import br.ufsc.gsigma.services.resilience.support.SOAService;

public class WarmupOrderingProcessServices {

	public static void main(String[] args) {

		boolean allAvailable = false;

		System.out.println("Warming up services of Ordering Process");

		while (!allAvailable) {

			boolean b = true;

			for (int i = 1; i <= 5; i++) {

				String host = "ublservices" + (i > 1 ? i : "");
				int port = 11000 + (i > 1 ? i - 1 : 0);

				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/buyerParty/placeOrder", "http://ubl.oasis.services/ordering/orderingprocess/buyerParty/placeOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/buyerParty/acceptOrder", "http://ubl.oasis.services/ordering/orderingprocess/buyerParty/acceptOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/buyerParty/rejectOrder", "http://ubl.oasis.services/ordering/orderingprocess/buyerParty/rejectOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/buyerParty/changeOrder", "http://ubl.oasis.services/ordering/orderingprocess/buyerParty/changeOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/buyerParty/cancelOrder", "http://ubl.oasis.services/ordering/orderingprocess/buyerParty/cancelOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/buyerParty/receiveResponse", "http://ubl.oasis.services/ordering/orderingprocess/buyerParty/receiveResponse")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/sellerParty/receiveOrder", "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/receiveOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/sellerParty/processOrder", "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/sellerParty/acceptOrder", "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/acceptOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/sellerParty/rejectOrder", "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/rejectOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/sellerParty/addDetail", "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/sellerParty/cancelOrder", "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/cancelOrder")) && b;
				b = isServiceAlive(new SOAService("http://" + host + ".d-201603244.ufsc.br:" + port + "/services/ubl/orderingprocess/sellerParty/changeOrder", "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/changeOrder")) && b;
			}

			allAvailable = b;
		}

		System.out.println("All services of Ordering Process are available");

	}

	private static boolean isServiceAlive(SOAService s) {

		try {
			if (ResilienceUtil.isServiceAlive(s)) {
				// System.out.println("Service " + s.getServiceEndpointURL() + " is ALIVE");
				return true;
			} else {
				System.out.println("Service " + s.getServiceEndpointURL() + " is NOT ALIVE");
				return false;
			}

		} finally {
			// try {
			// Thread.sleep(100L);
			// } catch (Exception e) {
			// }
		}
	}

}
