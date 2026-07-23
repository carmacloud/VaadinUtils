/*
* Copyright <2022> <bsutton@onepub.dev>
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, 
* including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished 
* to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
* OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package au.com.vaadinutils.flow.dao;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Use for ParentCrud DataProvider implementation.
 *
 * @param <E> The bean.
 */
public interface GenericDao<E, K> {

    void persist(E currentEntity);

    public E findById(K id);

    void refresh(E currentEntity);

    void commitAndContinue();

    void flush();

    E merge(E currentEntity);

    EntityTransaction getTransaction();

    void remove(E entity);

    List<E> findAll();

    List<E> findAll(SingularAttribute<E, ?> order[]);

    List<E> findAllByIds(SingularAttribute<E, Long> idAttribute, Collection<K> idsToFind);

}
