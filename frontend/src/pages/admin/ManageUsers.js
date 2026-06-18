import React, { useEffect, useState, useCallback } from 'react';
import API from '../../api';
import { toast } from 'react-toastify';
import { FaUsers, FaBan, FaCheck, FaTrash } from 'react-icons/fa';

export default function ManageUsers() {
  const [users, setUsers] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('');

  const load = useCallback(async (p = page) => {
    const params = { page: p, per_page: 15 };
    if (search) params.search = search;
    if (filter) params.role = filter;
    const r = await API.get('/admin/users', { params });
    setUsers(r.data.users);
    setPagination(r.data.pagination);
    setPage(p);
  }, [page, search, filter]);

  useEffect(() => { load(1); }, [filter]); // eslint-disable-line

  const action = async (url, msg) => {
    try { await API.post(url); toast.success(msg); load(); } catch (e) { toast.error(e.response?.data?.error || 'Failed'); }
  };

  const deleteUser = async (id) => {
    if (!window.confirm('Delete this user permanently?')) return;
    try { await API.delete(`/admin/user/${id}`); toast.success('User deleted'); load(); } catch (e) { toast.error(e.response?.data?.error || 'Failed'); }
  };

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title"><FaUsers /> Manage Users</h1>

        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <form onSubmit={e => { e.preventDefault(); load(1); }} style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'end' }}>
            <div className="form-group" style={{ flex: 1, minWidth: '200px' }}>
              <label>Search</label>
              <input className="form-control" value={search} onChange={e => setSearch(e.target.value)} placeholder="Email or username..." />
            </div>
            <div className="form-group">
              <label>Role</label>
              <select className="form-control" value={filter} onChange={e => setFilter(e.target.value)}>
                <option value="">All</option>
                <option value="donor">Donor</option>
                <option value="patient">Patient</option>
                <option value="both">Both</option>
                <option value="admin">Admin</option>
              </select>
            </div>
            <button type="submit" className="btn btn-primary" style={{ height: 'fit-content' }}>Search</button>
          </form>
        </div>

        <div className="card">
          <div className="table-responsive">
            <table className="table">
              <thead><tr><th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.username}</td>
                    <td>{u.email}</td>
                    <td><span className="badge badge-primary">{u.role || 'none'}</span></td>
                    <td>
                      {u.is_deleted ? <span className="badge badge-secondary">Deleted</span>
                        : u.is_blocked ? <span className="badge badge-danger">Blocked</span>
                        : <span className="badge badge-success">Active</span>}
                    </td>
                    <td style={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap' }}>
                      {!u.is_admin && (
                        <>
                          {u.is_blocked ? (
                            <button className="btn btn-sm btn-success" onClick={() => action(`/admin/user/${u.id}/unblock`, 'Unblocked')}><FaCheck /></button>
                          ) : (
                            <button className="btn btn-sm btn-warning" onClick={() => action(`/admin/user/${u.id}/block`, 'Blocked')}><FaBan /></button>
                          )}
                          <button className="btn btn-sm btn-danger" onClick={() => deleteUser(u.id)}><FaTrash /></button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {pagination && pagination.pages > 1 && (
            <div className="pagination">
              {page > 1 && <button className="btn btn-sm btn-outline" onClick={() => load(page - 1)}>Previous</button>}
              <span style={{ padding: '0.5rem' }}>Page {page} of {pagination.pages}</span>
              {page < pagination.pages && <button className="btn btn-sm btn-outline" onClick={() => load(page + 1)}>Next</button>}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
