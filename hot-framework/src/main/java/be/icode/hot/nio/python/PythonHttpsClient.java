package be.icode.hot.nio.python;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.python.core.PyDictionary;
import org.python.core.PyFunction;
import org.springframework.core.convert.support.DefaultConversionService;

import be.icode.hot.nio.HttpsClient;
import be.icode.hot.nio.python.PythonHttpClient.PyRequestCanceledClosure;
import be.icode.hot.nio.python.PythonHttpClient.PyRequestClosure;
import be.icode.hot.nio.python.PythonHttpClient.PyRequestErrorClosure;
import be.icode.hot.nio.python.PythonHttpClient.PyResponse;

public class PythonHttpsClient extends HttpsClient<PyFunction,PyDictionary> {

	public PythonHttpsClient(ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(eventLoopPool, defaultConversionService);
	}

	public PythonHttpsClient(ExecutorService bossExecutorService, ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(bossExecutorService, eventLoopPool, defaultConversionService);
	}
	
	public class PyHttpsRequest extends HttpsRequest {

		public PyHttpsRequest(Map<String, Object> options, PyFunction requestClosure) {
			super(options, requestClosure);
		}

		@Override
		protected RequestClosure<PyFunction, PyDictionary> buildRequestClosure(PyFunction requestClosure) {
			return new PyRequestClosure(requestClosure);
		}

		@Override
		protected RequestErrorClosure<PyFunction> buildRequestErrorClosure(PyFunction requestErrorClosure) {
			return new PyRequestErrorClosure(requestErrorClosure);
		}

		@Override
		protected RequestCanceledClosure<PyFunction> buildRequestCanceledClosure(PyFunction requestCanceledClosure) {
			return new PyRequestCanceledClosure(requestCanceledClosure);
		}
		
	}

	@Override
	protected Request buildRequest(PyDictionary options, PyFunction requestClosure) {
		return new PyHttpsRequest(options, requestClosure);
	}

	@Override
	protected Response<PyFunction,PyDictionary> buildResponse() {
		return new PyResponse();
	}
}
