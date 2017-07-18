/**
 * Description:  响应的结果对象类
 * <p><strong>服务接口基本规范：</strong></p>
 * <ol><li>任何服务方法，都必须使用<code>ServiceResult</code>返回结果；</li>
 * <li>客户端调用服务方法，得到<code>ServiceResult</code>对象；</li>
 * <li>{@link #getSuccess()}为true表示服务方法执行成功，通过{@link #getResult()} 得到执行结果； </li>
 * <li>{@link #getSuccess()}为false表示服务方法执行失败，通过{@link #getMessage()} 获取消息描述信息（错误信息）。</li></ol>
 * <p><strong>异常处理规范：</strong></p>
 * <ol><li>任何服务方法，不允许抛出异常。服务方法需要返回错误信息时，参考消息代码{@link #getCode()}、消息描述 {@link #getMessage()}
 * <ol type="A"><li>通过网络传递java异常本身是不可靠的事情，依赖于底层服务框架对异常堆栈进行序列化、反序列化以及传输处理，
 * 有些远程通讯框架会对异常进行二次封装处理；</li>
 * <li>某些异常堆栈信息可能存在循环引用等情况，会导致堆栈序列化失败；</li>
 * <li>服务端封装业务逻辑的实现，可能使用到各种第三方组件、库，处理过程中可能抛出各种自定义异常。而客户端则无需依赖这些第三方组件和库，
 * 这样对于某些异常，服务端序列化传递到客户端之后，客户端无法反序列化回来；</li></ol></li>
 * <li>客户端调用服务方法，必须使用try .. catch捕获异常；
 * OA中服务方法调用涉及到网络通讯，虽然服务方法承诺不会抛出任何异常，但网络通讯传输过程可能会出现各种异常信息， 甚至服务不可用等情况，
 * 客户端必须捕获异常并进行相应处理；</li></ol>
 * <p><strong>消息代码{@link #getCode()}、消息描述{@link #getMessage()}使用规范：</strong></p>
 * <ol><li>绝大部分服务方法不需要使用消息代码。
 * <p>如服务接口基本规范中所示，客户端通过 <code>getSuccess()</code>确定服务方法是否执行成功，如果执行失败，
 * 通过<code>getMessage()</code> 得到错误描述即可；</p>
 * <p>服务器端发生异常执行失败时，详细的错误描述、堆栈信息应当记录到数据库或者服务端的文件日志中，用于问题排查处理；
 * 然后通过<code>getMessage()</code> 返回对用户友好的、业务型的描述信息，客户端开发者通过这个信息可以大致了解、定位问题所在，
 * 详细的排查处理则通过服务端日志完成。</p>
 * <p>客户端可以选择直接或者根据场景稍加补充，在用户界面展示这个错误消息，向用户解释执行状况。</p></li>
 * <li>某些服务方法可能逻辑比较复杂，需要进行一系列比较重要的处理步骤，可能在不同步骤发生各种异常时，客户端需要有针对性的采取不同的处理方式，
 * 这种情况下可以使用消息代码。
 * <p>使用到消息代码的服务方法，必须在服务方法的JavaDoc中详细列出所有消息代码及解释。</p>
 * <p>对消息代码的编码不做进一步规范要求，各服务方法根据实际场景自行定义，但应该尽量采用英文字符，能比较准确的描述错误，
 * 看到消息代码时能够比较直观的理解所代表的错误类型。</p></li></ol>
 * Copyright: Copyright (Gogtz Corporation)2017
 * Company: Gogtz Corporation
 *
 * @author: t
 * @version: 1.0
 * Created at: 2017年7月17日15:26:21
 * Modification History:
 * Modified by :
 */
package com.gogtz.common.bean;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 响应的结果对象类
 * <p><strong>服务接口基本规范：</strong></p>
 * <ol><li>任何服务方法，都必须使用<code>ServiceResult</code>返回结果；</li>
 * <li>客户端调用服务方法，得到<code>ServiceResult</code>对象；</li>
 * <li>{@link #getSuccess()}为true表示服务方法执行成功，通过{@link #getResult()} 得到执行结果； </li>
 * <li>{@link #getSuccess()}为false表示服务方法执行失败，通过{@link #getMessage()} 获取消息描述信息（错误信息）。</li></ol>
 * <p><strong>异常处理规范：</strong></p>
 * <ol><li>任何服务方法，不允许抛出异常。服务方法需要返回错误信息时，参考消息代码{@link #getCode()}、消息描述 {@link #getMessage()}
 * <ol type="A"><li>通过网络传递java异常本身是不可靠的事情，依赖于底层服务框架对异常堆栈进行序列化、反序列化以及传输处理，
 * 有些远程通讯框架会对异常进行二次封装处理；</li>
 * <li>某些异常堆栈信息可能存在循环引用等情况，会导致堆栈序列化失败；</li>
 * <li>服务端封装业务逻辑的实现，可能使用到各种第三方组件、库，处理过程中可能抛出各种自定义异常。而客户端则无需依赖这些第三方组件和库，
 * 这样对于某些异常，服务端序列化传递到客户端之后，客户端无法反序列化回来；</li></ol></li>
 * <li>客户端调用服务方法，必须使用try .. catch捕获异常；
 * OA中服务方法调用涉及到网络通讯，虽然服务方法承诺不会抛出任何异常，但网络通讯传输过程可能会出现各种异常信息， 甚至服务不可用等情况，
 * 客户端必须捕获异常并进行相应处理；</li></ol>
 * <p><strong>消息代码{@link #getCode()}、消息描述{@link #getMessage()}使用规范：</strong></p>
 * <ol><li>绝大部分服务方法不需要使用消息代码。
 * <p>如服务接口基本规范中所示，客户端通过 <code>getSuccess()</code>确定服务方法是否执行成功，如果执行失败，
 * 通过<code>getMessage()</code> 得到错误描述即可；</p>
 * <p>服务器端发生异常执行失败时，详细的错误描述、堆栈信息应当记录到数据库或者服务端的文件日志中，用于问题排查处理；
 * 然后通过<code>getMessage()</code> 返回对用户友好的、业务型的描述信息，客户端开发者通过这个信息可以大致了解、定位问题所在，
 * 详细的排查处理则通过服务端日志完成。</p>
 * <p>客户端可以选择直接或者根据场景稍加补充，在用户界面展示这个错误消息，向用户解释执行状况。</p></li>
 * <li>某些服务方法可能逻辑比较复杂，需要进行一系列比较重要的处理步骤，可能在不同步骤发生各种异常时，客户端需要有针对性的采取不同的处理方式，
 * 这种情况下可以使用消息代码。
 * <p>使用到消息代码的服务方法，必须在服务方法的JavaDoc中详细列出所有消息代码及解释。</p>
 * <p>对消息代码的编码不做进一步规范要求，各服务方法根据实际场景自行定义，但应该尽量采用英文字符，能比较准确的描述错误，
 * 看到消息代码时能够比较直观的理解所代表的错误类型。</p></li></ol>
 *
 * @author: t
 * @version: 1.0
 * @time 2017年7月18日11:29:08
 */
public class ServiceResult<T> implements Serializable {
    private static final long serialVersionUID = -8824585616330663966L;
    /**
     * 返回给客户端的结果
     */
    private T result;

    /**
     * 返回给客户端的消息描述
     */
    private String message;

    /**
     * 返回给客户端的消息代码
     */
    private String code;

    /**
     * 执行成功标志
     */
    private Boolean success;

    public ServiceResult() {

    }

    /**
     * ServiceResult的构造函数
     *
     * @param success 操作标志
     * @param message 信息
     */
    public ServiceResult(final Boolean success, final String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * ServiceResult的构造函数
     *
     * @param success 操作标志
     * @param message 信息
     * @param result  结果
     */
    public ServiceResult(final Boolean success, final String message, final T result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    /**
     * 获取返回给客户端的结果
     *
     * @return 返回给客户端的结果
     */
    public T getResult() {
        return result;
    }

    /**
     * 设置返回给客户端的结果
     *
     * @param result 新的结果
     */
    public void setResult(final T result) {
        this.result = result;
    }

    /**
     * 获取返回给客户端的消息
     *
     * @return 返回给客户端的消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置返回给客户端的消息
     *
     * @param message 返回给客户端的消息
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * 获取执行成功标志
     *
     * @return Boolean 执行成功标志
     */
    public Boolean isSuccess() {
        return success;
    }

    /**
     * 获取执行成功标志
     *
     * @return Boolean 执行成功标志
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * 设置执行成功标志
     *
     * @param success 新的执行成功标志
     */
    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    /**
     * 获取返回给客户端的消息代码
     *
     * @return String 返回给客户端的消息代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置返回给客户端的消息代码
     *
     * @param code 新的消息代码
     */
    public void setCode(final String code) {
        this.code = code;
    }

    /**
     * 服务执行过程发生异常时设置错误信息。
     * <p>该方法会将 success设置为false（服务执行失败）</p>
     *
     * @param code    返回给客户端的消息代码
     * @param message 返回给客户端的消息描述
     */
    public void setError(final String code, final String message) {
        this.code = code;
        this.message = message;
        this.success = false;
    }

    /**
     * 服务执行过程发生异常时设置错误信息。
     * <p>该方法会将 success设置为false（服务执行失败）</p>
     *
     * @param message 返回给客户端的消息描述
     */
    public void setError(final String message) {
        this.code = StringUtils.EMPTY;
        this.message = message;
        this.success = false;
    }

    /**
     * 以JSON形式返回
     *
     * @return toString JSON字符串
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return toJson();
    }

    /**
     * 以JSON形式返回
     *
     * @return toString JSON字符串
     */
    public String toJson() {
        return JSONObject.toJSONString(this);
    }
}
