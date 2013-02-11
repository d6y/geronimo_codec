package org.example

import javax.servlet._
import javax.servlet.http._
import java.io._

import org.apache.commons.codec.binary.Base64

class MyFilter extends Filter {

val ignore =  (new Base64).decode("hi".getBytes("UTF-8"))

def init(config: FilterConfig) : Unit = {
}

def destroy() : Unit = {
}

def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) : Unit = {

	response.asInstanceOf[HttpServletResponse].setHeader("X-Seen", "1")
	chain.doFilter(request, response)
}

}
